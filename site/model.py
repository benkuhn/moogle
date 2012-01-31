from elixir import *

metadata.bind = "sqlite:///tunes.sqlite"
#metadata.bind.echo = True

class Tune(Entity):
    id=Field(Integer, primary_key=True)
    title=Field(String(100))
    abc=Field(Text())
    notes=Field(Text())
    abcsnippet=Field(String(200))

setup_all()
