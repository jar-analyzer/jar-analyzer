package log

import (
	"io"
	"os"
)

const (
	DebugLevel = iota
	InfoLevel
	WarnLevel
	ErrorLevel
	Disabled
)

func SetLevel(level int) {
	mu.Lock()
	defer mu.Unlock()

	for _, logger := range loggers {
		logger.SetOutput(os.Stdout)
	}

	if ErrorLevel < level {
		errorLog.SetOutput(io.Discard)
	}
	if WarnLevel < level {
		warnLog.SetOutput(io.Discard)
	}
	if InfoLevel < level {
		infoLog.SetOutput(io.Discard)
	}
	if DebugLevel < level {
		debugLog.SetOutput(io.Discard)
	}
}
