# lein-miditest

A Leiningen plugin to play a midi tone/sequence of sounds whenever tests or
retests have finished.

## Usage

Since this little plugin only adds midi tones to the `test` and `retest` tasks,
there's not much to do really. As I assume that you would like to add tones to
all tests used in Leiningen, putting it in your `:user` profile within
`~/.lein/profiles.clj` would do what you want. Put `[lein-miditest "0.1.0"]` in
the `:plugins` vector inside your user profile. If you've never modified the
`~/.lein/profiles.clj` file before, create it and put the following clojure
snippet into it:

```clj
{:user
 {:plugins [[lein-miditest "0.1.0"]]}}
```

If you want to put this into a `project.clj`, every single contributor will have
midi sounds playing whenever they test code with Leiningen. If you want a midi
choir, so be it, but take it into consideration if you want contributors.

That should be everything. `lein-miditest` doesn't come with any commands, just
use `lein test` and `lein retest` as you'd usually do.

## License

Copyright © 2013 by Jean Niklas L'orange.

Distributed under the [Eclipse Public License, version 1.0][license]. You can
find a copy in the root of this repository with the name `LICENSE`.

[license]: http://www.eclipse.org/legal/epl-v10.html "Eclipse Public License, version 1.0"
