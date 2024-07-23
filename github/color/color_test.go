package color

import (
	"strings"
	"testing"
)

func TestColor(t *testing.T) {
	const (
		yellowCode = "\033[33m"
		greenCode  = "\033[32m"
		blueCode   = "\033[34m"
		redCode    = "\033[31m"
		resetCode  = "\033[0m"
	)

	tests := []struct {
		function func(format string, a ...interface{}) string
		color    string
	}{
		{YellowSprintf, yellowCode},
		{GreenSprintf, greenCode},
		{BlueSprintf, blueCode},
		{RedSprintf, redCode},
	}

	for _, test := range tests {
		result := test.function("%s", "test")
		if !strings.Contains(result, test.color) {
			t.Errorf("Expected color code %s in result, got %s", test.color, result)
		}
		if !strings.HasSuffix(result, resetCode) {
			t.Errorf("Expected reset code at the end of result, got %s", result)
		}
	}

	YellowPrintln("test")
	GreenPrintln("test")
	BluePrintln("test")
	RedPrintln("test")

	YellowPrintf("test: %s\n", "1")
	GreenPrintf("test: %s\n", "1")
	BluePrintf("test: %s\n", "1")
	RedPrintf("test: %s\n", "1")

	t.Log("Color test completed")
}
