package log

import (
	"log"
	"os"
	"sync"
)

var (
	errorLog = log.New(os.Stdout, "\033[31m[error]\033[0m ", log.Ltime|log.Lshortfile)
	infoLog  = log.New(os.Stdout, "\033[32m[info ]\033[0m ", log.Ltime|log.Lshortfile)
	debugLog = log.New(os.Stdout, "\033[34m[debug]\033[0m ", log.Ltime|log.Lshortfile)
	warnLog  = log.New(os.Stdout, "\033[33m[warn ]\033[0m ", log.Ltime|log.Lshortfile)
	loggers  = []*log.Logger{errorLog, infoLog}
	mu       sync.Mutex
)

var (
	Error  = errorLog.Println
	Errorf = errorLog.Printf
	Warn   = warnLog.Println
	Warnf  = warnLog.Printf
	Info   = infoLog.Println
	Infof  = infoLog.Printf
	Debug  = debugLog.Println
	Debugf = debugLog.Printf
)
