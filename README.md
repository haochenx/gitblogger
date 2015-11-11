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

### Get started

To get Git Blogger running, clone the repository, and run `./gradlew run` to
bootstrap the server. You should be able to find the HTTP server running at
localhost:4567 after Gradle sets everything up.
