//go:build windows

package color

import (
	"syscall"
	"unsafe"
)

// Windows 系统修改 ENABLE_VIRTUAL_TERMINAL_PROCESSING
func isWindowsColorSupported() bool {
	var (
		kernel32           = syscall.NewLazyDLL("kernel32.dll")
		procGetConsoleMode = kernel32.NewProc("GetConsoleMode")
		procSetConsoleMode = kernel32.NewProc("SetConsoleMode")
	)
	var mode uint32
	stdoutHandle := uintptr(syscall.Stdout)
	r, _, _ := procGetConsoleMode.Call(stdoutHandle, uintptr(unsafe.Pointer(&mode)))
	if r == 0 {
		return false
	}
	// ENABLE_VIRTUAL_TERMINAL_PROCESSING
	newMode := mode | 0x0004
	r, _, _ = procSetConsoleMode.Call(stdoutHandle, uintptr(newMode))
	if r == 0 {
		return false
	}
	return true
}
