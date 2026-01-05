/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package db

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"jar-analyzer-mcp/pkg/log"
	"jar-analyzer-mcp/pkg/model"
	"os"
	"path/filepath"

	_ "modernc.org/sqlite"
)

var db *sql.DB

func InitDB() error {
	cwd, err := os.Getwd()
	if err != nil {
		return err
	}
	dbPath := filepath.Join(cwd, "jar-analyzer-report.db")

	log.Infof("init database: %s", dbPath)

	// Add busy_timeout and journal_mode=WAL to fix SQLITE_BUSY error
	// _pragma=busy_timeout(5000): Wait up to 5000ms for the lock
	// _pragma=journal_mode(WAL): Enable Write-Ahead Logging for better concurrency
	dsn := fmt.Sprintf("%s?_pragma=busy_timeout(5000)&_pragma=journal_mode(WAL)", dbPath)
	db, err = sql.Open("sqlite", dsn)
	if err != nil {
		return err
	}

	// Limit to 1 open connection to further prevent locking issues if WAL is not enough
	db.SetMaxOpenConns(1)

	createTableSQL := `CREATE TABLE IF NOT EXISTS vul_report (
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		type TEXT,
		reason TEXT,
		score INTEGER,
		trace TEXT,
		created_at DATETIME DEFAULT CURRENT_TIMESTAMP
	);`

	_, err = db.Exec(createTableSQL)
	return err
}

func SaveReport(report model.ReportData) error {
	if db == nil {
		return nil
	}
	traceJSON, err := json.Marshal(report.Trace)
	if err != nil {
		return err
	}

	insertSQL := `INSERT INTO vul_report (type, reason, score, trace) VALUES (?, ?, ?, ?)`
	_, err = db.Exec(insertSQL, report.Type, report.Reason, report.Score, string(traceJSON))
	return err
}

func GetReports() ([]model.ReportData, error) {
	if db == nil {
		return []model.ReportData{}, nil
	}

	rows, err := db.Query("SELECT type, reason, score, trace FROM vul_report ORDER BY id DESC")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var reports []model.ReportData
	for rows.Next() {
		var r model.ReportData
		var traceStr string
		if err := rows.Scan(&r.Type, &r.Reason, &r.Score, &traceStr); err != nil {
			return nil, err
		}
		if err := json.Unmarshal([]byte(traceStr), &r.Trace); err != nil {
			return nil, err
		}
		reports = append(reports, r)
	}
	return reports, nil
}

func CloseDB() {
	if db != nil {
		db.Close()
	}
}
