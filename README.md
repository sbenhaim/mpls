# mpls

Live code Max and Max4Live with Clojure.


## Prereqs

- Max
- Leiningen

## Setting Up

Download `mpls.jar` and copy it into the java lib folder for Max.

Mac: `/Applications/Max 6.1/Cycling '74/java/lib

### Doing Stuff

Launch Max/Max4Live and create an `mxj` object to load `mpls`.

[Image here]

Create a `nrepl start` message box and connect it to your `mxj` box.

Exit out of edit mode and click your `nrepl start` object.

You should see a message that says `nrepl server running on port [port]`

Open a term and enter

```
lein repl :connect localhost:[port]
```

using `port` from above.

You should now have a Clojure repl. (You can also use your repl-supporting Clojure editor to connect. Cider is supported with cider-nrepl v0.9.0-SNAPSHOT)

Import some stuff

```
(require '[mpls :refer :all])
```

Announce yourself

```
(hello-mpls!)
```

And define some action

```
(defn bang [this] (println "pew! pew! pew!"))
```

Wire a bang object to your `mxj` object and bang it. Congratulations, you're live-coding Max.

## Outlets

Send a message to an outlet like this:

```
(out "message")
```

To see your message, create a message box, wire the first outlet of `mxj mpls` to the second inlet of the message box, then do

```
(out "hello, there")
```

See the magic.

## Tying it together

Create a bang object and wire it to the first inlet of `mxj mpls`
Create a integer box and wire the first `mxj mpls` outlet to integer inlet

Then

```
(def n (atom 0))
(defn bang [this] (out (swap! n inc)))
```

Bang away!


## int-msg

int object -> mxj mpls -> int object

```
(defn int-msg [this i] (out (Math/pow i 2)))
```

## Programmatic layout

## Connecting things

## Read the code!

`mpls` is small, small. You can learn about many things that aren't
documented by taking a look at the code.

## License

Copyright © 2013-2015 Selah Ben-Haim
Distributed under the Eclipse Public License, the same as Clojure.

TODO: Remove max object from uberjar
