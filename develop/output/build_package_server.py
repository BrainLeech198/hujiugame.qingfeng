#!/usr/bin/env python3
"""氢风 文件下载服务器 — 双击运行，在局域网内分享打包产物"""

import http.server
import os
import socket
import sys

PORT = 8000


def get_local_ips():
    """获取本机局域网 IPv4 地址列表"""
    ips = []
    try:
        hostname = socket.gethostname()
        for info in socket.getaddrinfo(hostname, None):
            addr = info[4][0]
            # 只保留 IPv4 地址
            if ":" in addr:
                continue
            # 常见私有网段：10.x.x.x, 172.16-31.x.x, 192.168.x.x
            if addr.startswith("192.168.") or addr.startswith("10."):
                pass
            elif addr.startswith("172."):
                second = addr.split(".")[1]
                if second.isdigit() and 16 <= int(second) <= 31:
                    pass
                else:
                    continue
            else:
                continue
            if addr not in ips:
                ips.append(addr)
    except Exception:
        pass
    return ips


def main():
    # 切换工作目录到脚本所在目录，确保双击运行时正确提供打包产物
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    ips = get_local_ips()
    print("=" * 50)
    print("   氢风 文件下载服务器")
    print("=" * 50)
    print()
    print(f"   本机地址: http://localhost:{PORT}")
    for ip in ips:
        print(f"   局域网:    http://{ip}:{PORT}")
    if not ips:
        print("   (未检测到局域网 IP，请在虚拟机中手动输入本机 IP)")
    print()
    print("   按 Ctrl+C 停止服务器")
    print("=" * 50)
    print()

    handler = http.server.SimpleHTTPRequestHandler

    # 静默日志，只输出访问路径
    class QuietHandler(handler):
        def log_message(self, format, *args):
            print(f"   [{self.address_string()}] {args[0]} {args[1]} {args[2]}")

    try:
        server = http.server.HTTPServer(("0.0.0.0", PORT), QuietHandler)
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n服务器已停止")
    except OSError as e:
        print(f"错误: 端口 {PORT} 被占用: {e}")
        print(f"请检查是否有其他程序占用了该端口")
    finally:
        try:
            input("按 Enter 键退出...")
        except (EOFError, OSError):
            pass


if __name__ == "__main__":
    main()
