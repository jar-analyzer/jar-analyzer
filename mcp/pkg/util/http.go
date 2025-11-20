/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package util

import (
	"fmt"
	"io"
	"jar-analyzer-mcp/pkg/conf"
	"net/http"
	"net/url"
	"time"
)

var (
	client = &http.Client{Timeout: 15 * time.Second}
)

func HTTPGet(path string, params url.Values) (string, error) {
	u, err := url.Parse(conf.GlobalJarAnalyzerUrl)
	if err != nil {
		return "", fmt.Errorf("invalid base url: %w", err)
	}
	u.Path = path
	if params != nil {
		u.RawQuery = params.Encode()
	}
	req, err := http.NewRequest("GET", u.String(), nil)
	if err != nil {
		return "", err
	}
	if conf.JarAnalyzerAuth {
		req.Header.Add("Token", conf.JarAnalyzerToken)
	}
	resp, err := client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	b, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}
	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("http %d: %s", resp.StatusCode, string(b))
	}
	return string(b), nil
}
