package com.go2wheel.mysqlbackup.aop;

import java.util.concurrent.locks.Lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.exception.JobOnGoingException;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.Box;

@Aspect
@Component
public class TaskLockAspect {
	
	//https://blog.espenberntsen.net/2010/03/20/aspectj-cheat-sheet/
	
//	@Pointcut("execution(public * *(..))")
	
//	execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern)
//            throws-pattern?)
	
	
	@Around("execution(@com.go2wheel.mysqlbackup.aop.Exclusive * *(..)) && @annotation(exclusive)")
	public Object myAdvice(ProceedingJoinPoint proceedingJoinPoint, Exclusive exclusive) throws Throwable{
		Box box = (Box) proceedingJoinPoint.getArgs()[1];
		Lock lock = TaskLocks.getBoxLock(box.getHost(), exclusive.value());
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
	
//	@Pointcut("execution(* @com.go2wheel.mysqlbackup.aop。Exclusive *.*(..))")
//	@Pointcut("execution(* com.go2wheel.mysqlbackup.borg.BorgTaskFacade.*(..))")
//	@Pointcut("execution(public * *(..))")
//	@Pointcut("@annotation(com.go2wheel.mysqlbackup.aop.Exclusive")
//	public void exclusiveMethod() {}

}
