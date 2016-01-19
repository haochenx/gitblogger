<!-- -*- mode: markdown; fill-column: 80 -*- -->

# Repository for Git Blogger

## Project goal

The Git Blogger project is purposed to provide an easy to use blog system built
on Git The Stupid Content Tracker. It has the following goals:

* it should be VERY easy to get started with Git Blogger

* with Git Blogger, you shouldn't need to compile source files (e.g. markdown
  files) to HTML's manually

* with Git Blogger, you shouldn't need to maintain boilerplate information
  (e.g. article list) manually

* with Git Blogger, you should be able to refer resources (e.g. internal links,
  images, etc.) declaratively, with the smell of git (do we really want this
  smelling?)

* with Git Blogger, you should have complete control over what's going on

## First attempt

Useful thing evolves to be great gradually, usually starting negligibly. As so
Git Blogger starts with its very simple first attempt.

The first attempt is built on the [Spark Framework](http://sparkjava.com), with
Gradle as the build system, and support markdown (.md, will be processed with
[pegdown](https://github.com/sirthias/pegdown)) and HTML (.html) for page
sources. it will serve files from the current working directory as well the git
repository if existing at `$(pwd)/.git`, with the following URL scheme.

### Development mode and Production mode

Git Blogger will have 2 modes, the Development Mode (dev-mode) and Production
Mode (prod-mode). In dev-mode, all toplevel namespaces (/worktree, /index,
/object etc) will be enabled and the /exposed namespace will be mapped to
/index/browse for easy testing. But in production mode, only the contents that
would be served under /exposed is exposed at /, the URL root.

### Toplevel URL scheme

* /_ah/:path - ah, these are the internal pages (acronym of Administration Home)

* /worktree/:reqpath - the (supposed to be) blog site served with the files in
  the working tree

* /index/:reqpath - the (supposed to be) blog site served with the files in
  the git index

* /refs/:ref/:reqpath - the (supposed to be) blog site served with the files on
  the head of :ref, which is a git ref, e.g. heads/master

* /commit/:objectid/:reqpath - the (supposed to be) blog site served with the
  files on the commit :objectid

* /tree/:objectid/:reqpath - the (supposed to be) blog site served with the
  files on the git tree object :objectid

* /exposed/:reqpath - configurable nameplace, which will be mapped accordingly
  to configuration (configuration mechanism not yet though). it will be mapped
  to /index/browse/:reqpath in Development Mode, and to
  /refs/heads/master/browse/:reqpath in Production Mode by default.

* /object/:objectid?as=:mime - the content of the git object, served with the
  content type :mime. if the "as" parameter is missing, "text/plain" will be
  assumed.

### :reqpath scheme

* /raw/:path - will be mapped to the raw contents of :path, relative to the root
  of the repository.

* /view/:path - will be mapped to a rendered version of :path, relative to the
  root of the repository.

* /browse/:path - will try forwarding to (in the exact order):

    1. /view/:path
	1. /view/:path.html
	1. /view/:path.md
	1. /browser/:path/index (if :path ends with "/", it will be ignored)

## Get started

You can get GitBlogger running on your machine easily either with a release or
with the source code. As GitBlogger is written in Java, you need to first ensure
that Java Runtime Environment 8 (JRE 8) or higher is available on your system.
Either way you use, you should be able to find the GitBlogger HTTP server
serving at <http://localhost:4567> (with the default configuration) after
starting up.

### With a release

After downloading and extracting a release zip file, you should have a
`gitblogger/` directory. In the `gitblogger/bin/` sub-directory, there are
executables for *nix and Windows systems.

### With source code

To get Git Blogger running, clone the repository, and run `./gradlew run` to
bootstrap the server.

### Configurations

#### Running with executables in a release

You can pass "-help" to the executables to get a list of available options. For
the meanings of the options, please refer to the *Running with gradle*
subsection.

#### Running with gradle

You can configure Git Blogger passing it JVM properties. For example, if you're
using Gradle to bootstrap the server, the following command

	./gradlew -Dgitblogger.listeningPort=8080 -Dgitblogger.production run

will set `gitblogger.listeningPort` to `8080`, and enable the
`gitblogger.production` flag, which cause Git Blogger to run in production mode
at the port 0.0.0.0.

Here's a list of the properties you can used to configure the behavior of Git
Blogger:

* `gitblogger.rootRepo`

    use this property to specify the git repository dir of which contents Git
    Blogger should server. it is defaulted to `$(pwd)`

* `gitblogger.bareRootRepo`

	use this property to specify the git repository dir of which contents Git
	Blogger should server. using this property also make Git Blogger treat the
	repository as bare. note that this property will surpass
	`gitblogger.rootRepo`.

* `gitblogger.rootExposedRef`

    this property specify which ref should Git Blogger server in the production
    mode. if not specified, it is defaulted to `refs/master/master` (i.e. the
    master branch)

* `gitblogger.production`

    use this flag to tell Git Blogger to run in Production Mode. if this flag is
    absent, Git Blogger will run in Development Mode. note that this is a flag
    that its presence is significant, so setting its value to "false" will not
    turn it off (i.e. if you want Git Blogger to be run in Development Mode,
    don't pass this property at all).

* `gitblogger.listeningIp`

	use this property to specify on which IP address Git Blogger should be
	listening on. it is defaulted to `0.0.0.0`

* `gitblogger.listeningPort`

	use this property to specify on which TCP port Git Blogger should be listening
	on. it is defaulted to `4567`

* `gitblogger.canonicalUrl`

	use this property to specify the default URL for Git Blogger to be accessed
	through. it will be used to generate links that can be referred from outside
	the blog, and it will be used to derive the URL root location Git Blogger
	should use to serve its contents. it is defaulted to nothing.
