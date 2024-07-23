package color

import (
	"fmt"
	"os"
	"runtime"
)

var (
	Red    = "\033[31m"
	Green  = "\033[32m"
	Yellow = "\033[33m"
	Blue   = "\033[34m"
	Reset  = "\033[0m"
)

func DisableColor() {
	Red = ""
	Green = ""
	Yellow = ""
	Blue = ""
	Reset = ""
}

func IsSupported() bool {
	if runtime.GOOS != "windows" {
		term, ok := os.LookupEnv("TERM")
		if !ok || term == "dumb" {
			return false
		}
		return true
	}
	// enable color terminal in windows
	return isWindowsColorSupported()
}

func RedPrintln(a ...interface{}) {
	fmt.Println(Red + fmt.Sprint(a...) + Reset)
}

func GreenPrintln(a ...interface{}) {
	fmt.Println(Green + fmt.Sprint(a...) + Reset)
}

func YellowPrintln(a ...interface{}) {
	fmt.Println(Yellow + fmt.Sprint(a...) + Reset)
}

func BluePrintln(a ...interface{}) {
	fmt.Println(Blue + fmt.Sprint(a...) + Reset)
}

func RedPrintf(format string, a ...interface{}) {
	fmt.Printf(Red+format+Reset, a...)
}

func GreenPrintf(format string, a ...interface{}) {
	fmt.Printf(Green+format+Reset, a...)
}

func YellowPrintf(format string, a ...interface{}) {
	fmt.Printf(Yellow+format+Reset, a...)
}

func BluePrintf(format string, a ...interface{}) {
	fmt.Printf(Blue+format+Reset, a...)
}

func RedSprintf(format string, a ...interface{}) string {
	return fmt.Sprintf(Red+format+Reset, a...)
}

func GreenSprintf(format string, a ...interface{}) string {
	return fmt.Sprintf(Green+format+Reset, a...)
}

func YellowSprintf(format string, a ...interface{}) string {
	return fmt.Sprintf(Yellow+format+Reset, a...)
}

func BlueSprintf(format string, a ...interface{}) string {
	return fmt.Sprintf(Blue+format+Reset, a...)
}
