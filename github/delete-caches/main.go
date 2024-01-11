package main

import (
	"context"
	"fmt"
	"net"
	"net/http"
	"os"
	"strconv"

	"github.com/google/go-github/v57/github"
	"golang.org/x/net/proxy"
	"golang.org/x/oauth2"
)

const socksProxy = "127.0.0.1:10808"

func main() {
	data, err := os.ReadFile("token.txt")
	if err != nil {
		fmt.Println(err)
		return
	}
	token := string(data)

	repoOwner := "jar-analyzer"
	repoName := "jar-analyzer"

	ctx := context.Background()
	ts := oauth2.StaticTokenSource(
		&oauth2.Token{AccessToken: token},
	)
	tc := oauth2.NewClient(ctx, ts)

	dialer, err := proxy.SOCKS5("tcp",
		socksProxy, nil, proxy.Direct)
	if err != nil {
		fmt.Println("Error creating dialer:", err)
		return
	}
	t := tc.Transport.(*oauth2.Transport)
	t.Base = &http.Transport{
		DialContext: func(ctx context.Context, network, addr string) (net.Conn, error) {
			return dialer.Dial(network, addr)
		},
	}
	tc.Transport = t

	client := github.NewClient(tc)

	caches, _, err := client.Actions.ListCaches(
		ctx, repoOwner, repoName, &github.ActionsCacheListOptions{})
	if err != nil {
		fmt.Println(err)
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
			fmt.Printf("delele %s success\n",
				strconv.FormatInt(*cache.ID, 10))
		}
	}
}
