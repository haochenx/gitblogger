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
sources. The first attempt doesn't have any git related feature specified yet.
it will just serve files from the current working directory, with the following
URL scheme.

### URL scheme

* /raw/:path - will be mapped to the raw contents of :path, relative to the root
  of the repository.

* /view/:path - will be mapped to a rendered version of :path, relative to the
  root of the repository.

* /browse/:path - will try forwarding to (in the exact order):

    1. /view/:path
	1. /view/:path.html
	1. /view/:path.md
	1. /browser/:path/index (if :path ends with "/", it will be ignored)

* / - will be mapped to /browse/

### Get started

To get Git Blogger running, clone the repository, and run `./gradlew run` to
bootstrap the server. You should be able to find the HTTP server running at
localhost:4567 after Gradle sets everything up.
