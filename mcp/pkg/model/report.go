/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package model

type ReportData struct {
	Type   string  `json:"type"`
	Reason string  `json:"reason"`
	Score  int8    `json:"score"`
	Trace  []Trace `json:"trace"`
}

type Trace struct {
	Class  string `json:"class"`
	Method string `json:"method"`
}
