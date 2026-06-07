import socket

SERVER_IP = "10.0.0.239"
SERVER_PORT = 34175


def send_command(command: str) -> str:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(5)
        s.connect((SERVER_IP, SERVER_PORT))
        s.sendall((command + "\r\n").encode("utf-8"))
        response = s.makefile("r").readline()
        return response


def main():
    print(f"Connecting to {SERVER_IP}:{SERVER_PORT}")
    print()

    command = "getenc"
    print(f">>> {command}")

    raw = send_command(command)

    print(f"Raw response : {repr(raw)}")
    print(f"Stripped     : {repr(raw.strip())}")

    # Try to parse as integer
    try:
        value = int(raw.strip())
        print(f"Parsed int   : {value}")
    except ValueError:
        print("Could not parse as integer — not a plain number")

        # Try common formats like "enc:12345" or "pos=12345"
        for sep in [":", "=", " "]:
            if sep in raw:
                parts = raw.strip().split(sep)
                print(f"  Split by '{sep}': {parts}")
                try:
                    value = int(parts[-1].strip())
                    print(f"  Last part as int: {value}")
                except ValueError:
                    pass


if __name__ == "__main__":
    main()
