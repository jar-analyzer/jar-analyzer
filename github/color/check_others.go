//go:build !windows

package color

// 其他操作系直接返回
func isWindowsColorSupported() bool {
	return true
}
