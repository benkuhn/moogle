import process, sys, socket

def search(nums):
    sock = socket.create_connection(('localhost', 16180))
    sock.recv(16) #cmd>
    sock.sendall('search\n')
    sock.recv(16) #notes>
    sock.sendall(' '.join([str(num) for num in nums]) + '\n')
    resp = []
    tmp = None
    while True:
        tmp = sock.recv(4096)
        if tmp.endswith('\ncmd>'):
            tmp = tmp[:-6]
            resp.append(tmp)
            break
        else:
            resp.append(tmp)
    sock.sendall('bye\n')
    sock.close()
    return ''.join(resp)

if __name__ == "__main__":
    abc = sys.argv[1]
    (notes, chords, meta, _, __) = process.parse(abc)
    print search(notes)
