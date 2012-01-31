import sys, re, uuid, model, traceback, socket
from collections import defaultdict

match_keysig = re.compile(r"K:(.*?)$", re.MULTILINE)
#TODO: handle first and second repeats
match_barline = re.compile(r"[\[\]|:]+(\n[\[\]|:]+)?")
#TODO handle P correctly
#TODO words
match_meta = re.compile(r"([A-Z]):(.*?)$", re.MULTILINE)
match_note = re.compile(r"""(^^|^|__|_|=|) # accidental
                            ([a-gA-G])     # note
                            (,|'|)         # octave marker
                            \d*(?:/\d*)?   # length (discarded)
                            """, re.X)
match_chord1 = re.compile(r'"(.*?)"')
match_comment = re.compile(r"%.*$", re.MULTILINE)
note_num = {'C':15, 'D':17, 'E':19, 'F':20, 'G':22, 'A':24, 'B':26, 
            'c':27, 'd':29, 'e':31, 'f':32, 'g':34, 'a':36, 'b':38, }
acc_num = {'__':-2, '_':-1, '=':0, '':0, '^':1, '^^':2}
oct_num = {',':-12, '\'':12, '':0}

def parse_note(score, keysig=defaultdict(lambda:0)):
    m = match_note.match(score)
    if m == None:
        return (None, None, score)
    else:
        acc = m.group(1)
        note = m.group(2)
        if acc == '':
            accn = keysig[note.lower()]
        else:
            accn = acc_num[acc]
        num = note_num[note] + accn + oct_num[m.group(3)]
        return (num, m.group(0), score[m.end():])

def parse(score):
    notes = []
    chords = []
    meta = {}
    snip_index = -1
    abc = []
    def doappend(string):
        abc.append(string)
    barlines = 0
    keysig = defaultdict(lambda: 0)
    while score != "":
        #try to read a keysig
        m = match_keysig.match(score)
        if m != None:
            keysig = parse_keysig(m.group(1).strip())
            score = score[m.end():]
            doappend(m.group(0))
            continue
        m = match_meta.match(score)
        if m != None:
            #TODO process this better
            key = m.group(1)
            val = m.group(2)
            if key in meta:
                if key == 'T': sep = ' / '
                else: sep = '\n'
                meta[key] = meta[key] + sep + val
            meta[key] = val
            #the regex doesn't hold onto newlines, so we add them in manually
            if m.group(1) in ['L', 'M']:
                doappend(m.group(0) + '\n')
            score = score[m.end()+1:]
            continue
        #try to read a note
        n, chunk, rest = parse_note(score, keysig)
        if n != None:
            notes.append(n)
            score = rest
            doappend(chunk)
            continue
        #try to read a chord
        m = match_chord1.match(score)
        if m != None:
            #TODO legit chord parsing
            chords.append(m.group(1))
            score = score[m.end():]
            doappend(m.group(0))
            continue
        m = match_barline.match(score)
        if m != None:
            #NOTE: this will miscount pieces with an initial barline.
            #oh well, close enough
            doappend(m.group(0))
            barlines += 1
            if barlines == 3:
                snip_index = len(abc)
            score = score[m.end():]
            continue
        m = match_comment.match(score)
        if m != None:
            #maybe add 1 to m.end()?
            score = score[m.end():]
            continue
        #give up and try again
        doappend(score[0])
        score = score[1:]
    abcfinal = ''.join(abc)
    abcsnip = ''.join(abc[:snip_index])
    return (notes, chords, meta, abcfinal, abcsnip)

sharps_by_note = {'C':0, 'G':1, 'D':2, 'A':3, 'E':4, 'B':5, 'F#':6, 'C#':7,
                  'F':-1, 'Bb':-2, 'Eb':-3, 'Ab':-4, 'Db':-5, 'Gb':-6, 'Cb':-7}
sharps_by_mode = {'maj':0, 'ion':0, 'm':-3, 'min':-3, 'dor':-2, 'phr':-4, 'lyd':1,
                  'mix':-1, 'aeo':-3, 'loc':-5, '':0}
sharps = ['f', 'c', 'g', 'd', 'a', 'e', 'b']
flats = sharps[::-1]
re_keysig = re.compile(r"([A-G][b#]?)\s*([a-zA-Z]*)")
#WARNING: this can't deal with double-accidental sigs like G# or Cb mix.
#TODO global accidentals
def parse_keysig(abc):
    k = re_keysig.match(abc)
    sig = defaultdict(lambda: 0)
    if k is None:
        if abc == 'HP':
            return sig
        elif abc == "Hp":
            sig['f'] = 1
            sig['c'] = 1
            sig['g'] = 0
            return sig
        else:
            raise Exception("malformed keysig: '" + abc + "'")
    else:
        note = k.group(1)
        mode = k.group(2)[:3].lower()
        n = sharps_by_note[note] + sharps_by_mode[mode]
        if n < 0:
            for f in flats[0:-n]:
                sig[f] = -1
        else:
            for s in sharps[0:n]:
                sig[s] = 1
    return sig

KGRAM = 4
def intervals(notes):
    return [notes[x+1] - notes[x] for x in xrange(len(notes)-1)]
def ngrams(notes):
    return [intervals(notes[x:x+KGRAM+1]) for x in xrange(len(notes)-KGRAM)]

def each_tune(fname, func):
    with open(fname) as abc:
        tunes = abc.read().strip().split("X:")[1:] #get rid of first part w/ no X:
    i = 0
    total = len(tunes)
    for tune in tunes:
        i += 1
        tune = tune.strip().lstrip('1234567890\n') #get rid of X: line
        func(i, tune, total)

def single_tune(i):
    return model.Tune.query.filter_by(id = i).first()

def tune2db(i, tune, total):
    try:
        (notes, chords, meta, abc, snip) = parse(unicode(tune, "UTF-8"))
    except:
        traceback.print_exc()
        return
    if 'T' in meta:
        title = meta['T'].strip()
    else:
        title = 'no title'
    notes_text = ' '.join([str(x) for x in notes]) + '\n'
    t = model.Tune(title=title, abc=abc, notes=notes_text, abcsnippet=snip)
    print '.',
    return t

def db2index():
    sock = socket.create_connection(('localhost', 16180))
    print sock.recv(10)
    for o in model.Tune.query.all():
        sock.send('add\n')
        r1 = sock.recv(10)
        #print r1,
        sock.send(str(o.id) + '\n')
        r2 = sock.recv(10)
        #print r2,
        sock.send(o.notes)
        r3 = sock.recv(10)
        #print r3,
        print '.',
    sock.send('bye\n')
    print sock.recv(10)

def print_snips(i, tune, total):
    try:
        (notes, chords, meta, abc, snip) = parse(tune)
    except:
        traceback.print_exc()
        return
    print snip

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
        if tmp.endswith('cmd>'):
            tmp = tmp[:-6]
            resp.append(tmp)
            break
        else:
            resp.append(tmp)
    sock.sendall('bye\n')
    sock.close()
    return ''.join(resp)

def align(i, nums):
    sock = socket.create_connection(('localhost', 16180))
    sock.recv(16) #cmd>
    sock.sendall('align\n')
    sock.recv(16) #id>
    sock.sendall(str(i) + '\n')
    sock.recv(16) #notes>
    sock.sendall(' '.join([str(num) for num in nums]) + '\n')
    resp = []
    tmp = None
    while True:
        tmp = sock.recv(4096)
        if tmp.endswith('cmd>'):
            tmp = tmp[:-4]
            resp.append(tmp)
            break
        else:
            resp.append(tmp)
    sock.sendall('bye\n')
    sock.close()
    return ''.join(resp)

if __name__ == "__main__":
    err = open("err.txt", "w")
    cmd = sys.argv[1]
    if cmd in ["db:create", "ccrunch", "cindex"]:
        model.create_all()
    if cmd in ["crunch", "ccrunch", "index", "cindex"]:
        import os
        with open("data/.log") as log:
            already_written = log.read().split()
        for fname in os.listdir("data/"):
            if fname.endswith(".abc") and fname not in already_written:
                already_written.append(fname)
                print fname
                each_tune("data/" + fname, tune2db)
                model.session.commit()
        with open("data/.log", "w") as log:
            log.write('\n'.join(already_written))
        
    if cmd in ["index", "db2ind"]:
        db2index()
    if cmd == "printsnips":
        each_tune(sys.argv[2], print_snips)
    if cmd == "search":
        abc = sys.argv[2]
        notes = parse(abc)[0]
        text = search(notes)
        upper = 1000
        if len(sys.argv) >= 4:
            upper = int(sys.argv[3])
            print upper
        for line in text.split():
            i, score = line.split(':')[:2]
            score = int(score)
            if score > upper:
                break
            tune = single_tune(i)
            print score, tune.title.encode("utf-8"), "({0})".format(i)
            #print tune.title, ':', score
    if cmd == "tune":
        i = int(sys.argv[2])
        tune = single_tune(i)
        print tune.title
        print tune.abc
        print tune.notes
    if cmd == "align":
        abc = sys.argv[2]
        notes = parse(abc)[0]
        i = sys.argv[3]
        print align(i, notes)
    if cmd == "parse":
        abc = sys.argv[2]
        notes = parse(abc)[0]
        print ' '.join([str(i) for i in notes])
    if cmd == "tgrep":
        grep = "%{0}%".format(sys.argv[2])
        for tune in model.Tune.query.filter(model.Tune.title.like(grep)):
            print tune.id, tune.title
