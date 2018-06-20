package com.go2wheel.mysqlbackup.aop;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.exception.JobOnGoingException;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.JobLogDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;

@Aspect
@Component
public class TaskLockAspect {
	
	//https://blog.espenberntsen.net/2010/03/20/aspectj-cheat-sheet/
	
//	@Pointcut("execution(public * *(..))")
	
//	execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern)
//            throws-pattern?)
	
	@Autowired
	private JobLogDbService jobLogDbService;
	
	
	@Around("execution(@com.go2wheel.mysqlbackup.aop.Exclusive * *(..)) && @annotation(exclusive)")
	public Object myAdvice(ProceedingJoinPoint proceedingJoinPoint, Exclusive exclusive) throws Throwable{
		Server server = (Server) proceedingJoinPoint.getArgs()[1];
		Lock lock = TaskLocks.getBoxLock(server.getHost(), exclusive.value());
		if (lock.tryLock()) {
			try {
				return proceedingJoinPoint.proceed();
			} finally {
				lock.unlock();
			}
		} else {
			throw new JobOnGoingException();
		}
	}
	
	@Around("execution(@com.go2wheel.mysqlbackup.aop.MeasureTimeCost * *(..))")
	public Object measureAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
		long startTime = System.currentTimeMillis();
		Object o = proceedingJoinPoint.proceed();
		if (o instanceof TimeCost) {
			((TimeCost)o).setStartTime(startTime);
			((TimeCost)o).setEndTime(System.currentTimeMillis());
		}
		return o;
	}
	
	
	@Around("execution(@com.go2wheel.mysqlbackup.aop.TrapException * *(..)) && @annotation(trapException)")
	public void jobExceptionAdvice(ProceedingJoinPoint proceedingJoinPoint, TrapException trapException) throws Throwable{
		JobExecutionContext context = (JobExecutionContext) proceedingJoinPoint.getArgs()[0]; 
		try {
			proceedingJoinPoint.proceed();
		} catch (Throwable e) {
			saveException(e, trapException, context);
			throw e;
		}
	}

	private void saveException(Throwable e, TrapException trapException, JobExecutionContext context) {
		JobLog jl = new JobLog();
		JobDataMap data = context.getMergedJobDataMap();
		Properties p = new Properties();
		p.putAll(data);
		
		jl.setCtx(p.toString());
		jl.setJobClass(trapException.value().getName());
		jl.setCreatedAt(new Date());
		jl.setExp(ExceptionUtil.stackTraceToString(e));
		jobLogDbService.save(jl);
	}
	
//	@Pointcut("execution(* @com.go2wheel.mysqlbackup.aop。Exclusive *.*(..))")
//	@Pointcut("execution(* com.go2wheel.mysqlbackup.borg.BorgTaskFacade.*(..))")
//	@Pointcut("execution(public * *(..))")
//	@Pointcut("@annotation(com.go2wheel.mysqlbackup.aop.Exclusive")
//	public void exclusiveMethod() {}

}
