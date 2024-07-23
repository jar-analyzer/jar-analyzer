package util

import (
	"context"
	"fmt"
	"net"
	"net/http"
	"strconv"

	"github.com/google/go-github/v57/github"
	"golang.org/x/net/proxy"
	"golang.org/x/oauth2"

	"github.com/jar-analyzer/jar-analyzer/github/color"
)

func CleanCache(token string, useProxy bool, socks string, repoOwner string, repoName string) {
	ctx := context.Background()
	ts := oauth2.StaticTokenSource(
		&oauth2.Token{AccessToken: token},
	)
	tc := oauth2.NewClient(ctx, ts)
	if useProxy {
		dialer, err := proxy.SOCKS5("tcp",
			socks, nil, proxy.Direct)
		if err != nil {
			color.RedPrintln(err)
			return
		}
		t := tc.Transport.(*oauth2.Transport)
		t.Base = &http.Transport{
			DialContext: func(ctx context.Context, network, addr string) (net.Conn, error) {
				return dialer.Dial(network, addr)
			},
		}
		tc.Transport = t
	}
	client := github.NewClient(tc)
	caches, _, err := client.Actions.ListCaches(
		ctx, repoOwner, repoName, &github.ActionsCacheListOptions{})
	if err != nil {
		color.RedPrintln(err)
		return
	}
	for _, cache := range caches.ActionsCaches {
		key := *cache.Key
		resp, err := client.Actions.DeleteCachesByKey(
			ctx, repoOwner, repoName, key, nil)
		if err != nil {
			fmt.Println(err)
			return
		}
		if resp.StatusCode == 200 {
			color.YellowPrintf("delele %s success\n",
				strconv.FormatInt(*cache.ID, 10))
		}
	}
}
