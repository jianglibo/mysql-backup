## Spring的多模板技术。

为了厘清这个解析过程，还是需要一些小知识和小技巧。但最关键的一点是谁能找到模板！按照这个思路来看不同的配置。

### 配置不同的后缀
比如ftl->freemarker, html -> thymeleaf。当查找a（注意不是a.ftl或者a.html）模板时，freemarker接到这个解析请求时，将a -> a.ftl，找到a.ftl就解析完成， 由freemark来处理了。如果没找到，thymeleaf将a -> a.html，同样的步骤。这种方案存在的问题是无法自由选择freemarker或者thymeleaf。

### 配置不同的前缀
ftldir -> freemarker, htmldir -> thymeleaf。 **在同时配置不同后缀的情况下**， 那么请求a（不是a.ftl或者a.html）时，freemarker就会查找ftldir/a.ftl， thymeleaf就会查找htmldir/a.html，先到先得，同样无法自由选择模板。但是在 **不配置后缀** 的情况下，指定a.ftl，肯定是freemarker接管，指定a.html就会由thymeleaf接管。 因为只有在ftldir下面才有ftl结尾的模板。




