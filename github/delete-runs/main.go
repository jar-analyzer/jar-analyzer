package main

import (
	"context"
	"fmt"
	"net"
	"net/http"
	"os"

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
	action := "maven.yml"

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

	runs, _, err := client.Actions.ListWorkflowRunsByFileName(
		ctx, repoOwner, repoName, action, &github.ListWorkflowRunsOptions{})
	if err != nil {
		fmt.Println(err)
		return
	}

	for _, run := range runs.WorkflowRuns {
		req, err := http.NewRequest("DELETE", run.GetURL(), nil)
		if err != nil {
			fmt.Println(err)
			continue
		}
		req.Header.Set("Authorization", "token "+token)
		req.Header.Set("Accept", "application/vnd.github.v3+json")

		resp, err := http.DefaultClient.Do(req)
		if err != nil {
			fmt.Println(err)
			continue
		}
		_ = resp.Body.Close()

		if resp.StatusCode == http.StatusNoContent {
			fmt.Printf("run %d got deleted\n", run.GetID())
		}
	}
}
