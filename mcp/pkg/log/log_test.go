package log

import (
	"fmt"
	"testing"
)

func TestSetLevel(t *testing.T) {
	fmt.Println("test debug level")
	SetLevel(DebugLevel)
	Debug("test info")
	Debugf("test infof: %s", "test")
	Info("test error")
	Infof("test errorf: %s", "test")
	Warn("test warn")
	Warnf("test warnf: %s", "test")
	Error("test error")
	Errorf("test errorf: %s", "test")

	fmt.Println("test info level")
	SetLevel(InfoLevel)
	Debug("test info")
	Debugf("test infof: %s", "test")
	Info("test error")
	Infof("test errorf: %s", "test")
	Warn("test warn")
	Warnf("test warnf: %s", "test")
	Error("test error")
	Errorf("test errorf: %s", "test")

	fmt.Println("test warn level")
	SetLevel(WarnLevel)
	Debug("test info")
	Debugf("test infof: %s", "test")
	Info("test error")
	Infof("test errorf: %s", "test")
	Warn("test warn")
	Warnf("test warnf: %s", "test")
	Error("test error")
	Errorf("test errorf: %s", "test")

	fmt.Println("test error level")
	SetLevel(ErrorLevel)
	Debug("test info")
	Debugf("test infof: %s", "test")
	Info("test error")
	Infof("test errorf: %s", "test")
	Warn("test warn")
	Warnf("test warnf: %s", "test")
	Error("test error")
	Errorf("test errorf: %s", "test")

	fmt.Println("test disabled level")
	SetLevel(Disabled)
	Debug("test info")
	Debugf("test infof: %s", "test")
	Info("test error")
	Infof("test errorf: %s", "test")
	Warn("test warn")
	Warnf("test warnf: %s", "test")
	Error("test error")
	Errorf("test errorf: %s", "test")
}
