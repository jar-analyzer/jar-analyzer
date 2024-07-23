package main

import (
	"os"
	"strings"

	"github.com/jar-analyzer/jar-analyzer/github/color"
	"github.com/jar-analyzer/jar-analyzer/github/util"
)

var (
	useProxy   = false
	socksProxy = "127.0.0.1:10808"
)

func usage() {
	color.RedPrintln("USAGE: go run .\\github\\main.go proxy|no-proxy")
}

func main() {
	if !color.IsSupported() {
		color.DisableColor()
	}
	args := os.Args
	if len(args) != 2 {
		usage()
		return
	}
	if args[1] == "proxy" {
		color.GreenPrintln("OPTIONS: USE PROXY")
		useProxy = true
	} else if args[1] == "no-proxy" {
		color.GreenPrintln("OPTIONS: NOT USE PROXY")
		useProxy = false
	} else {
		usage()
		return
	}

	tokenBytes, err := os.ReadFile("token.txt")
	if err != nil {
		color.RedPrintln(err)
		return
	}

	token := strings.TrimSpace(string(tokenBytes))
	repoOwner := "jar-analyzer"
	repoName := "jar-analyzer"

	color.GreenPrintln("--- START GITHUB CACHES CLEAN ---")
	util.CleanCache(token, useProxy, socksProxy, repoOwner, repoName)

	color.GreenPrintln("--- START GITHUB ACTION BUILDS CLEAN ---")
	util.CleanAction(token, useProxy, socksProxy, repoOwner, repoName, "build.yml")

	color.GreenPrintln("--- START GITHUB ACTION LEAKS CLEAN ---")
	util.CleanAction(token, useProxy, socksProxy, repoOwner, repoName, "leak.yml")

	color.GreenPrintln("--- START GITHUB ACTION MAVENS CLEAN ---")
	util.CleanAction(token, useProxy, socksProxy, repoOwner, repoName, "maven.yml")

	color.GreenPrintln("--- START GITHUB ACTION TRUFFLEHOG CLEAN ---")
	util.CleanAction(token, useProxy, socksProxy, repoOwner, repoName, "trufflehog.yml")

    color.GreenPrintln("--- START GITHUB ACTION GOLANG CLEAN ---")
    util.CleanAction(token, useProxy, socksProxy, repoOwner, repoName, "golang.yml")

	color.GreenPrintln("GITHUB CLEAN FINISH")
}
