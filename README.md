# Atlassian Connect Spring Boot

This repository contains a [Spring Boot](http://projects.spring.io/spring-boot/) starter for building
[Atlassian Connect](https://connect.atlassian.com/) add-ons for JIRA and Confluence.

This is the **officially supported** Atlassian Connect Java framework. Please read our documentation to see our other supported and community provided [Frameworks and Tools](https://developer.atlassian.com/static/connect/docs/latest/developing/frameworks-and-tools.html). The tools listed in that documentation will greatly aid you while writing your Atlassian Connect add-on; we highly recommend you make use of them.

## Dependencies
 
 - [Apache Maven](http://maven.apache.org/)

## Features

`atlassian-connect-spring-boot-starter` provides the following features:

* Serving of the add-on descriptor (`atlassian-connect.json`) with configuration support
* Automatic handling of installation and uninstallation lifecycle callbacks
* JSON Web Token authentication for incoming requests
* JSON Web Token signing for outbound requests
* Persistence of hosts using [Spring Data](http://projects.spring.io/spring-data/)
* Conversion of [standard context parameters](https://developer.atlassian.com/static/connect/docs/latest/concepts/context-parameters.html#standard-parameters)
to [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html) model attributes

Additionally, `atlassian-connect-spring-boot-jpa-starter` provides bindings to [Spring Data JPA](http://projects.spring.io/spring-data-jpa/)
and [Liquibase](http://www.liquibase.org/).

## Getting started

To get started with the framework, there are two options. You can generate a fresh project using a Maven archetype, which
will set up the structure and dependencies for you. Alternatively if you have an existing Java project, you can manually
add a few things to your project to turn it into an Atlassian Connect add-on.

### Create a project from the Maven archetype

Execute the following command: 

    mvn archetype:generate -DarchetypeGroupId=com.atlassian.connect
        -DarchetypeArtifactId=atlassian-connect-spring-boot-archetype
        -DarchetypeVersion=1.0.0

Maven will ask you to define the `groupId`, `artifactId`, and `version` for your new project. You will also be asked to specify
the Java package for your source code, which by default is the same as your `groupId`. Once confirmed, Maven will generate
the source of a skeleton Atlassian Connect add-on, including:

 - a [JSON add-on descriptor](https://developer.atlassian.com/static/connect/docs/latest/modules/) (`atlassian-connect.json`)
 - a POM with the required dependencies
   - `atlassian-connect-spring-boot-starter`
   - `atlassian-connect-spring-boot-jpa-starter` with bindings to [Spring Data JPA](http://projects.spring.io/spring-data-jpa/) and [Liquibase](http://www.liquibase.org/)
 - an `application.yml` file to specify Spring properties
 - a [Spring Boot application class](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-spring-application.html)
 
### Modify an existing project

1. Set up your project using [Spring Boot](http://projects.spring.io/spring-boot/#quick-start) (but don't specify any
`@RequestMapping`s just yet - we'll get to that in a bit)
2. Add an extra dependency to your POM:

        <dependency>
            <groupId>com.atlassian.connect</groupId>
            <artifactId>atlassian-connect-spring-boot-starter</artifactId>
            <version>${atlassian-connect-spring-boot.version}</version>
        </dependency>

3. Choose a [Spring Data](http://projects.spring.io/spring-data/) implementation to use with `AtlassianHostRepository`,
and [enable `Repository` scanning](http://docs.spring.io/spring-data/data-commons/docs/1.12.2.RELEASE/reference/html/#repositories.create-instances.java-config)
with the appropriate `@Enable${store}Repositories` annotation. If you choose [Spring Data JPA](http://projects.spring.io/spring-data-jpa/),
consider using `atlassian-connect-spring-boot-jpa-starter`.
4. Create an [add-on descriptor](https://developer.atlassian.com/static/connect/docs/latest/modules/) file called 
`atlassian-connect.json` in the main resource directory of your application. The following is a minimal descriptor that
is installable in both JIRA and Confluence (but does nothing):

        {
          "key": "my-addon",
          "baseUrl": "http://localhost:8080",
          "name": "My add-on",
          "authentication": {
            "type": "jwt"
          },
          "lifecycle": {
            "installed": "/installed",
            "uninstalled": "/uninstalled"
          }
        }

## Run your project

Build your project, then run the following command:

    mvn spring-boot:run
    
Your application should start up locally on port 8080. If you visit `http://localhost:8080/atlassian-connect.json` in your
browser, you should see your add-on descriptor.

## Respond to requests to an endpoint

To implement an endpoint in your add-on, create a spring `Controller` class with a `RequestMapping` method. For example:

    @Controller
    public class HelloWorld {

        @RequestMapping(value = "/hello-world", method = RequestMethod.GET)
        @ResponseBody
        public String helloWorld(@AuthenticationPrincipal AtlassianHostUser hostUser) {
            return "hello-world";
        }
    }

This is mostly standard Spring. If you create a module in your add-on descriptor with `url` attribute "/hello-world", the product
will hit this endpoint and you can return whatever you need to. You can also inject information about the host product instance
that performed the request as a method parameter, as we've done in this example. The `AtlassianHostUser` class will also
contain the user key of the product user that triggered the request to your add-on, if any.

Endpoints specified in this way will automatically authenticate incoming requests using 
[JSON Web Tokens](https://developer.atlassian.com/static/connect/docs/latest/concepts/authentication.html).
To disable JWT verification for an endpoint, you can annotate your `RequestMapping` method or your `Controller` class with
`@IgnoreJwt`. You should only disable JWT verification for endpoints that will not be accessed by an Atlassian product.

## View rendering

Spring Boot supports [a number of web template languages](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-web-applications.html#boot-features-spring-mvc-template-engines).
Using a template language allows you to render dynamic content based on information that you receive in a request.
For example, the following endpoint takes the request parameter `"username"` and passes it on to be rendered by a template called `"hello"`.

    @RequestMapping(value = "/hello-world", method = RequestMethod.GET)
    public ModelAndView helloWorld(@RequestParam String username) {
        ModelAndView model = new ModelAndView();
        model.setViewName("hello");
        model.addObject("userName", username);
        return model;
    }

You could use then use this value in your template, for example in a Velocity template:

    <body>
        Hi $userName!
        ...
    </body>

Atlassian Connect Spring Boot provides a number of model attributes by default that you can use in your template:

 - `atlassian-connect-all-js-url`
 - `atlassian-connect-license`
 - `atlassian-connect-locale`
 - `atlassian-connect-timezone`
 
### Atlassian Connect JavaScript API

The [Atlassian Connect JavaScript client library](https://developer.atlassian.com/static/connect/docs/latest/concepts/javascript-api.html)
establishes a cross-domain messaging bridge between an Atlassian Connect iframe and the host product. All pages to be displayed 
within an Atlassian product must include a file called `all.js`, served from the product, in order to establish the bridge 
and be able to be displayed.

The URL to this file is provided by Atlassian Connect Spring Boot for every request. For example, using Velocity, you
would only need to add the following to your pages:

    <script src="$atlassian-connect-all-js-url" type="text/javascript"></script>

## Making API requests to the product

Atlassian Connect Spring Boot will automatically sign requests from your add-on to an installed host product with JSON
Web Tokens. To make a request, just autowire a `RestTemplate` object into your class. When responding to an incoming
request, outgoing requests can be made relative URL's will be made to the current authenticated

    @Autowired
    private RestTemplate restTemplate;
    
    public void doSomething() {
        restTemplate.getForObject("/rest/api/example", Void.class);

## Reacting to add-on lifecycle events

Upon successful completion of add-on installation or uninstallation, [a Spring application event](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html#context-functionality-events)
will be fired: `AddonInstalledEvent` or `AddonUninstalledEvent`. These events are fired asynchronously and cannot affect
the HTTP response returned to the Atlassian host.

## Configuration

You can use a [Spring properties file](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
to configure the behaviour of your application. If you define properties in your properties file, they will override the default
values set by Atlassian Connect Spring Boot.

* `atlassian.connect.allow-reinstall-missing-host`

Atlassian hosts will sign all but the first installation request. If your add-on loses the host details during
development, this flag enables installations to be accepted by your add-on.

* `atlassian.connect.debug-all-js`

The client library for the Atlassian Connect JavaScript API comes in two versions: an obfuscated version for production
use (`all.js`) and a plain version for development use (`all-debug.js`). This flag populates the
`atlassian-connect-all-js-url` Spring MVC model attribute with the development version.

### Making your add-on production ready

Some of the default configuration for Atlassian Connect Spring Boot is only safe in a development environment - you should enable
a [Spring profile](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html) called 
`production` if deploying to a production environment. There are a number of ways to set the active profiles of a Spring
Boot application. For example, if launching your application using the Spring Boot Maven plugin:

    mvn spring-boot:run -Drun.profiles=production

By default, your application will use an in-memory database, which is useful for development but inappropriate in a production
environment. You will need to point your application towards a production database in your properties file, e.g. to use
PostgreSQL you should add something like the following to your properties file:

    spring.jpa.database=POSTGRESQL
    spring.datasource.url=jdbc:postgresql://localhost:5432/my-database

For more information on working with SQL databases, see the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-sql.html).

## Useful Spring Boot dependencies

### Spring Boot Actuator

The [Spring Boot Actuator](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready) will add
a number of monitoring and management endpoints to your application. By default, Atlassian Connect Spring Boot moves these
endpoints to `/manage`. You can change this behaviour by setting the value of `management.context-path` in your properties
file.

### Spring Boot Developer Tools

Spring Boot provides a number of useful [developer tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html)
you can use. The [automatic restart](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html#using-boot-devtools-restart)
capabilities are particularly useful for developing add-ons.

## Getting help

If you need help using Spring Boot, see the [Getting help](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-documentation-getting-help)
section of the Spring Boot Reference Guide.

If you need help using functionality provided by Atlassian Connect Spring Boot, please post your question on
[Atlassian Answers](https://answers.atlassian.com/questions/ask?topics=atlassian-connect-spring-boot).

## Reporting a problem

Please raise any issues in the [ACSPRING](https://ecosystem.atlassian.net/browse/ACSPRING) project on Atlassian Ecosystem JIRA.

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE.txt).