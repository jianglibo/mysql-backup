package com.go2wheel.mysqlbackup.commands;

import com.go2wheel.mysqlbackup.AppEventListenerBean;
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.SecurityService;
import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.annotation.CandidatesFromSQL;
import com.go2wheel.mysqlbackup.annotation.DbTableName;
import com.go2wheel.mysqlbackup.annotation.SetServerOnly;
import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;
import com.go2wheel.mysqlbackup.annotation.TemplateIndicator;
import com.go2wheel.mysqlbackup.dbservice.GlobalStore;
import com.go2wheel.mysqlbackup.dbservice.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.dbservice.PlayBackService;
import com.go2wheel.mysqlbackup.dbservice.ReusableCronDbService;
import com.go2wheel.mysqlbackup.dbservice.SqlService;
import com.go2wheel.mysqlbackup.exception.InvalidCronExpressionFieldException;
import com.go2wheel.mysqlbackup.exception.NoActionException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder.CronExpressionField;
import com.go2wheel.mysqlbackup.job.MailerJob;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil.UpgradeFile;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.ConfigFile;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;
import com.go2wheel.mysqlbackup.value.Server;
import com.go2wheel.mysqlbackup.value.Subscribe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.validation.constraints.Email;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent()
public class BackupCommand {

  public static final String DESCRIPTION_FILENAME = "description.yml";

  public static final String DANGEROUS_ALERT = "I know what i am doing.";

  public static final int RESTART_CODE = 101;

  @Autowired
  private SecurityService securityService;

  @Autowired
  private GlobalStore globalStore;

  @Autowired
  private MyAppSettings myAppSettings;

  @Autowired
  private SqlService sqlService;

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private SettingsInDb settingsInDb;

  @Autowired
  private Environment environment;

  @Autowired
  private MailerJob mailerJob;

  @Autowired
  private TemplateContextService templateContextService;

  @Autowired
  @Lazy
  private SchedulerService schedulerService;

  @Autowired
  private ReusableCronDbService reusableCronDbService;

  @Autowired
  private AppEventListenerBean appEventListenerBean;

  @Autowired
  private UserGroupLoader userGroupLoader;

  @Autowired
  private LocaledMessageService localedMessageService;

  @Autowired
  private ConfigFileLoader configFileLoader;

  @ShellMethod(value = "获取CPU的核数")
  public FacadeResult<?> serverCoreNumber(
      @ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server) throws RunRemoteCommandException, UnExpectedInputException {

    // int i = serverStateService.getCoreNumber(server, sas.getSession());
    // return FacadeResult.doneExpectedResultDone(i);
    return null;
  }

  @ShellMethod(value = "将数据库在目标服务器上重建。")
  public FacadeResult<?> serverAddDbPair(@SetServerOnly @ShellOption(help = "模拟的SET类型的服务器") Server server) throws IOException {
    return FacadeResult.doneExpectedResultDone(server);
  }

  @ShellMethod(value = "将文件目录在目标服务器上重建。")
  public FacadeResult<?> serverAddDirPair(@SetServerOnly @ShellOption(help = "模拟的SET类型的服务器") Server server,
      @ShellOption(help = "模拟路径") Server dir) throws IOException {
    return FacadeResult.doneExpectedResultDone(server);
  }

  @ShellMethod(value = "显示配置相关信息。")
  public List<String> systemInfo(@ShellOption(help = "环境变量名", defaultValue = "") String envname) throws IOException {
    if (StringUtil.hasAnyNonBlankWord(envname)) {
      return Arrays.asList(String.format("%s: %s", envname, environment.getProperty(envname)));
    }
    return Arrays.asList(formatKeyVal("database url", environment.getProperty("spring.datasource.url")),
        formatKeyVal("working directory", Paths.get("").toAbsolutePath().normalize().toString()),
        formatKeyVal("download directory", settingsInDb.getDownloadPath().normalize().toAbsolutePath().toString()),
        formatKeyVal("log file", environment.getProperty("logging.file")),
        formatKeyVal("spring.config.name", environment.getProperty("spring.config.name")),
        formatKeyVal("spring.config.location", environment.getProperty("spring.config.location")),
        formatKeyVal("myapp.psapp", myAppSettings.getPsappPath().toAbsolutePath().toString()),
        formatKeyVal("myapp.psdataDir", myAppSettings.getPsdataDirPath().toAbsolutePath().toString()),
        formatKeyVal("myapp.chromeexec", myAppSettings.getChromeexec()),
        formatKeyVal("Spring active profile", String.join(",", environment.getActiveProfiles())));
  }

  @ShellMethod(value = "Exit the shell.", key = { "quit", "exit" })
  public void quit(@ShellOption(help = "退出值", defaultValue = "0") int exitValue,
      @ShellOption(help = "重启") boolean restart) {
    if (restart) {
      System.exit(101);
    }
    System.exit(exitValue);
  }

  @ShellMethod(value = "Upgrade system.")
  public FacadeResult<?> systemUpgrade(@ShellOption(help = "新版本的zip文件") File zipFile) throws UnExpectedInputException {
    Path zp = zipFile.toPath();
    if (!Files.exists(zp)) {
      return FacadeResult.showMessageExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, zipFile);
    }
    try {
      UpgradeUtil uu = new UpgradeUtil(zp);
      boolean writed = uu.writeUpgradeFile();
      if (!writed) {
        UpgradeFile uf = uu.getUpgradeFile();
        if (uf != null) {
          return FacadeResult.showMessageExpected("command.upgrade.degrade", uf.getNewVersion(),
              uf.getCurrentVersion());
        } else {
          return FacadeResult.showMessageExpected("command.upgrade.degrade", "unknown", "unknown");
        }
      }
    } catch (IOException e) {
      ExceptionUtil.logErrorException(logger, e);
      return FacadeResult.unexpectedResult(e, "command.upgrade.failed");
    }
    quit(0, true);
    return null;
  }

  private String formatKeyVal(String k, String v) {
    return String.format("%s: %s", k, v);
  }

  @ShellMethod(value = "列出配置文件列表")
  public FacadeResult<?> listConfigFile() throws IOException {
    List<String> ls = new ArrayList<>(configFileLoader.listConfigFiles().keySet());
    return FacadeResult.doneExpectedResultDone(ls);
  }

  @ShellMethod(value = "执行配置文件中的命令")
  public FacadeResult<?> runConfigFileCommand(@ShellOption(help = "Config file name.") ConfigFile configFile,
      @ShellOption(help = "cmd key in config file.") String psCmdKey)
      throws IOException, ExecutionException, NoActionException {
    ProcessExecResult pe = configFileLoader.runCommand(configFile.getMypath(), psCmdKey);
    List<String> lines = pe.getStdOut();
    lines.addAll(pe.getStdError());
    return FacadeResult.doneExpectedResultDone(lines);
  }

  @ShellMethod(value = "列出后台任务")
  public FacadeResult<?> asyncList() throws IOException {
    List<SavedFuture> gobjects = globalStore.getFutureGroupAll(BackupCommand.class.getName());

    List<String> ls = gobjects.stream()
        .map(sf -> String.format("Task %s, Done: %s", sf.getDescription(), sf.getCf().isDone()))
        .collect(Collectors.toList());
    return FacadeResult.doneExpectedResultDone(ls);

  }

  @ShellMethod(value = "添加常用的CRON表达式")
  public FacadeResult<?> cronExpressionAdd(@ShellOption(help = "cron表达式") String expression,
      @ShellOption(help = "描述", defaultValue = "") String description) {
    ReusableCron rc = new ReusableCron(expression, description);
    try {
      rc = reusableCronDbService.save(rc);
      return FacadeResult.doneExpectedResult(rc, CommonActionResult.DONE);
    } catch (Exception e) {
      return FacadeResult.showMessageUnExpected(CommonMessageKeys.MALFORMED_VALUE, rc.getExpression());
    }
  }

  @ShellMethod(value = "构建CRON表达式")
  public String cronExpressionBuild(
      @ShellOption(help = "支持的格式示例：1 或者 1,8,10 或者 5-25 或者 5/15(从5开始以15递增。)", defaultValue = "0") String second,
      @ShellOption(help = "格式同second参数，范围0-59", defaultValue = "0") String minute,
      @ShellOption(help = "格式同second参数，范围0-23", defaultValue = "0") String hour,
      @ShellOption(help = "格式同second参数，范围1-31", defaultValue = "?") String dayOfMonth,
      @ShellOption(help = "格式同second参数，范围1-12", defaultValue = "*") String month,
      @ShellOption(help = "格式同second参数，范围1-7，其中1是周六。", defaultValue = "*") String dayOfWeek,
      @ShellOption(help = "空白或者1970-2099", defaultValue = "") String year) {

    CronExpressionBuilder ceb = new CronExpressionBuilder();
    try {

      CronExpressionBuilder.validCronField(CronExpressionField.SECOND, second);
      ceb.second(second);

      CronExpressionBuilder.validCronField(CronExpressionField.MINUTE, minute);
      ceb.minute(minute);

      CronExpressionBuilder.validCronField(CronExpressionField.HOUR, hour);
      ceb.hour(hour);

      CronExpressionBuilder.validCronField(CronExpressionField.DAY_OF_MONTH, dayOfMonth);
      ceb.dayOfMonth(dayOfMonth);

      CronExpressionBuilder.validCronField(CronExpressionField.MONTH, month);
      ceb.month(month);

      CronExpressionBuilder.validCronField(CronExpressionField.DAY_OF_WEEK, dayOfWeek);
      ceb.dayOfWeek(dayOfWeek);
      CronExpressionBuilder.validCronField(CronExpressionField.YEAR, year);
      ceb.year(year);

      return ceb.build();
    } catch (InvalidCronExpressionFieldException e) {
      return e.getMessage();
    }
  }

  @ShellMethod(value = "列出常用的CRON表达式")
  public FacadeResult<?> cronExpressionList() {
    return FacadeResult.doneExpectedResult(reusableCronDbService.findAll(), CommonActionResult.DONE);
  }

  @ShellMethod(value = "用户列表。")
  public FacadeResult<?> listUsers() {
    return FacadeResult.doneExpectedResult(userGroupLoader.getAllUsers(), CommonActionResult.DONE);
  }

  @ShellMethod(value = "服务器组列表。")
  public FacadeResult<?> listServerGroups() {
    return FacadeResult.doneExpectedResult(userGroupLoader.getAllGroups(), CommonActionResult.DONE);
  }

  @ShellMethod(value = "列出用户和服务器组的关系。")
  public FacadeResult<?> listSubscribes() {
    List<Subscribe> vos = userGroupLoader.getAllSubscribes();
    return FacadeResult.doneExpectedResult(vos, CommonActionResult.DONE);
  }

  @ShellMethod(value = "加载测试数据。")
  public FacadeResult<?> loadData(@ShellOption(help = "加入计划任务") boolean schedule) throws Exception {
    appEventListenerBean.loadData(null, schedule, true);
    return FacadeResult.doneExpectedResult();
  }

  @ShellMethod(value = "支持的语言")
  public List<String> languageList() {
    List<String> las = new ArrayList<>();
    las.add(Locale.TRADITIONAL_CHINESE.getLanguage());
    las.add(Locale.ENGLISH.getLanguage());
    las.add(Locale.JAPANESE.getLanguage());
    return las;
  }

  @ShellMethod(value = "支持的语言")
  public String languageSet(String language) {
    if (!languageList().contains(language)) {
      return String.format("Supported languages are: %s", String.join(", ", languageList()));
    }
    Locale l = Locale.forLanguageTag(language);
    appEventListenerBean.setLocal(l);
    return "switch to language: " + language;
  }

  @ShellMethod(value = "列出当前主机的计划任务")
  public FacadeResult<?> listSchedulerJob(@ShellOption(help = "列出全部而不单单是当前主机。") boolean all) throws SchedulerException {
    return FacadeResult.doneExpectedResultDone(
        schedulerService.getAllJobKeys().stream().map(StringUtil::formatJobkey).collect(Collectors.toList()));
  }

  @ShellMethod(value = "查看最后一个命令的详细执行结果")
  public String facadeResultLast() {
    FacadeResult<?> fr = appEventListenerBean.getFacadeResult();
    if (fr == null) {
      return "";
    } else {
      if (fr.getException() != null) {
        return ExceptionUtil.stackTraceToString(fr.getException());
      } else if (fr.getResult() != null) {
        return fr.getResult().toString();
      } else if (fr.getMessage() != null && !fr.getMessage().isEmpty()) {
        return localedMessageService.getMessage(fr.getMessage());
      } else {
        return "";
      }
    }
  }

  @ShellMethod(value = "执行SQL SELECT.")
  public FacadeResult<?> sqlSelect(@DbTableName @ShellOption(help = "数据表名称") String tableName,
      @ShellOption(help = "返回的记录数", defaultValue="1   0 ") int limit) {
    return FacadeResult.doneExpectedResultDone(sqlService.select(tableName, limit));
  }

  @ShellMethod(value = "执行SQL DELETE.")
  public FacadeResult<?> sqlDelete(@DbTableName @ShellOption(help = "数据表名称") String tableName,
      @ShellOption(help = "记录的ID") int id) {
    return FacadeResult.doneExpectedResultDone(sqlService.delete(tableName, id));
  }

  @ShellMethod(value = "获取HSQLDB的CRYPT_KEY")
  public FacadeResult<?> securityKeygen(@ShellOption(help = "编码方式", defaultValue="   A ES") String enc) throws ClassNotFoundException, SQLException {
    return securityService.securityKeygen(enc);
  }

  @ShellMethod(value = "将SSHkey从文件复制到数据库或反之。")
  public FacadeResult<?> securityCopySshkey(@ShellOption(help = "db到文件") boolean toFile,
      @ShellOption(help = "删除ssh文件") boolean deleteFile) throws ClassNotFoundException, IOException {
    return securityService.securityCopySshkey(toFile, deleteFile);
  }

  @ShellMethod(value = "将KnownHosts从文件复制到数据库或反之。")
  public FacadeResult<?> securityCopyKnownHosts(@ShellOption(help = "db到文件") boolean toFile) throws ClassNotFoundException, IOException {
    return securityService.securityCopyKnownHosts(toFile);
  }

  @ShellMethod(value = "立即发送建邮件通知。")
  public FacadeResult<?> emailNoticeSend(
    @ShellOption(help = "邮件地址") @Email String email,
    @TemplateIndicator
    @ShellOption(help = "邮件模板") String template,
    @ShellOption(help = "用户服务器组") Subscribe subscribe,
    @ShellOption(help = "真的发送") boolean sendTruely
    ) throws ClassNotFoundException, IOException, MessagingException, ExecutionException {
    ServerGroupContext sgctx = templateContextService.createMailerContext(subscribe);
    if (sendTruely) {
      mailerJob.mail(subscribe, email, template, sgctx);
      return FacadeResult.doneExpectedResultDone("mail had sent to " + email + ".");
    } else {
      return FacadeResult.doneExpectedResultDone(mailerJob.renderTemplate(template, sgctx));
    }
  }

  @Autowired
  private PlayBackService playBackService;

  @ShellMethod(value = "创建回放设定。")
  public FacadeResult<?> playbackCreate(
    @ShellOption(help = "源服务器") Server sourceServer,
    @ShellOption(help = "回放服务器") Server targetServer,
    @ShowPossibleValue({PlayBack.PLAY_BORG, PlayBack.PLAY_MYSQL})
    @ShellOption(help = "回放内容") String playWhat,
    @ShellOption(help = "设定条目", defaultValue=ShellOption.NULL) List<String> settings
    ) throws UnExpectedInputException {
    PlayBack pb = playBackService.create(sourceServer, targetServer, playWhat, settings);
    return FacadeResult.doneExpectedResultDone(pb);
  }

  @ShellMethod(value = "删除回放设定。")
  public FacadeResult<?> playbackDelete(@ShellOption(help = "回放设定") PlayBack playback) {
    playBackService.remove(playback);
    return FacadeResult.doneExpectedResult();
  }

  @Autowired
  private KeyValueDbService keyValueDbService;

  public static final String KEY_VALUE_CANDIDATES_SQL = "SELECT ITEM_KEY FROM KEY_VALUE WHERE ITEM_KEY LIKE '%%%s%%'";

  @ShellMethod(value = "查询键值对。")
  public FacadeResult<?> keyValueGet(
      @CandidatesFromSQL(KEY_VALUE_CANDIDATES_SQL) @ShellOption(help = "键值，可以用dot分隔") String key) {
    List<KeyValue> kvs = keyValueDbService.findByKeyPrefix(key);
    return FacadeResult.doneExpectedResultDone(kvs);
  }

  @ShellMethod(value = "删除键值对。")
  public FacadeResult<?> keyValueDelete(
      @CandidatesFromSQL(KEY_VALUE_CANDIDATES_SQL) @ShellOption(help = "键值，可以用dot分隔") String key) {
    KeyValue kv = keyValueDbService.findOneByKey(key);
    if (kv == null) {
      return FacadeResult.doneExpectedResultPreviousDone(CommonMessageKeys.DB_ITEMNOTEXISTS);
    } else {
      keyValueDbService.delete(kv);
      return FacadeResult.doneExpectedResultDone(kv);
    }

  }

  @ShellMethod(value = "新建或更新键值对。")
  public FacadeResult<?> keyValueUpdateOrCreate(
      @CandidatesFromSQL(KEY_VALUE_CANDIDATES_SQL) @ShellOption(help = "键值，可以用dot分隔") String key,
      @ShellOption(help = "值") String value) {
    KeyValue kv = keyValueDbService.findOneByKey(key);
    if (kv != null) {
      if (!kv.getItemValue().equals(value)) {
        kv.setItemValue(value);
        kv = keyValueDbService.save(kv);
      }
    } else {
      kv = new KeyValue(key, value);
      kv = keyValueDbService.save(kv);
    }
    return FacadeResult.doneExpectedResultDone(kv);
  }

  @Bean
  public PromptProvider myPromptProvider() {
    return () -> new AttributedString(getPromptString(), AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
  }

  private String getPromptString() {
    return "no_server_selected> ";
  }
}
