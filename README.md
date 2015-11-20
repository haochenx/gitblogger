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

### Common Toplevel URL scheme

* /_ah/:path - ah, these are the internal pages (acronym of Administration
  Home), not implemented yet though

### Development Toplevel URL scheme

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

### Production Mode Toplevel URL scheme

* /:repoid/:path, if :repoid exists, will be mapped (effectively) to /refs/<the
  exposed ref of :repoid>/browse/:path under the :repoid's repo

* /:path will be mapped (effectively, since any internal URL is not exposed
  in Production Mode) to /refs/<gitblogger.rootExposedRef>/browse/:path

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

### Get started

To get Git Blogger running, clone the repository, and run `./gradlew run` to
bootstrap the server. You should be able to find the HTTP server running at
localhost:4567 after Gradle sets everything up.

### Configurations

You can configure Git Blogger passing it JVM properties. For example, if you're
using Gradle to bootstrap the server, the following command

	./gradlew -Dgitblogger.listeningPort=8080 -Dgitblogger.production run

will set `gitblogger.listeningPort` to `8080`, and enable the
`gitblogger.production` flag, which cause Git Blogger to run in production mode
at the port 0.0.0.0.

Here's a list of the properties you can used to configure the behavior of Git
Blogger:

* `gitblogger.root`

    use this property to specify the git repository dir of which contents Git
    Blogger should server. Git Blogger will try to detect whether it is a bare
    repository or a normal repository. it is defaulted to `$(pwd)`. you can
    also use the following format to specify which ref should be exposed when in
    Production Mode:

		/path/to/repo@exposing-ref

	note that this implies that you cannot have '@' in your repository path.

* `gitblogger.repos`

	use this properties to specify additional repositories that you want Git
    Blogger to serve together with the root repository, in the following format:

		a=repo_a_path@exposing-ref,b=repo_b_path@exposing-ref

	make sure not to include any space around the '='s and ','s.

* `gitblogger.production`

    use this flag to tell Git Blogger to run in Production Mode. if this flag is
    absent, Git Blogger will run in Development Mode. note that this is a flag
    that its presence is significant, so setting its value to "false" will not
    turn it off (i.e. if you want Git Blogger to be run in Development Mode,
    don't pass this property at all).

* `gitblogger.ip`

	use this property to specify on which IP address Git Blogger should be
	listening on. it is defaulted to `0.0.0.0`

* `gitblogger.port`

	use this property to specify on which TCP port Git Blogger should be listening
	on. it is defaulted to `4567`

* `gitblogger.canonicalUrl`

	use this property to specify the default URL for Git Blogger to be accessed
	through. it will be used to generate links that can be referred from outside
	the blog, and it will be used to derive the URL root location Git Blogger
	should use to serve its contents. it is defaulted to nothing.
