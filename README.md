# mpls

Live-code Max and Max4Live with Clojure.


## Prereqs

- [Max][https://cycling74.com/products/max/]
- [Leiningen][http://leiningen.org/]

## Setting Up

Download `mpls.jar` and copy it into the java lib folder for Max.

Mac: `/Applications/Max 6.1/Cycling '74/java/lib
Windows: TODO
Linux: TODO

### Doing Stuff

Launch Max/Max4Live and create an `mxj` object to load `mpls`.

[Image here]

Create a `nrepl start` message box and connect it to your `mxj` box.

Exit edit mode and click your `nrepl start` object.

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

Do you first thing

```
(post "Here I am!")
```

If you Max logging window is open (Max > Window > Max Window), you will see `Here I am!` printed there.

Now create a `bang` object and connect it to `mpls`.

Now bang! it.

You should see something like `user/bang unimplemented` in the Max window. Let's fix that.

Announce yourself, like a good rock star.

```
(hello-mpls!)
```

(`mpls` will dispatch messages sent to the `mpls` box to certain functions defined in the namespace that calls this function, if you care.)

Now define a function called `bang`.

```
(defn bang [this inlet] (post "pew! pew! pew!"))
```

Bang it again! Congratulations, you're live-coding Max.

## Outlets

Send a message to an outlet like this:

```
(out "message")
```

To see your message, create a message box, wire the first outlet of `mxj mpls` to the second inlet of the message box, then do

```
(out "hello, there")
```

Connect the second outlet to an integer box. Then try

```
(out 1 1024)
```

And this

```
(dotimes [i 1000] (out 1 i))
```

See the magic.

## Tying it together

```
(def n (atom 0))
(defn bang [this inlet] (out (swap! n inc)))
```

Bang away!

## Other messages

You've seen `bang`, which responds to bangs on the inlet. There is also `int-msg` for ints, `float-msg` for floats, `list-msg` for lists, `dblclick` for mousey things, and `msg` for everything else.

`bang` and `dblclick` take two arguments (`this` which is `mpls` : MaxObject, and the 0-indexed inlet number). `list-msg` and `msg` take three arguments, `this`, inlet, and a vector of args. `int-msg` and `float-msg` take three args, this, inlet, and a number.

Messages sent to `mpls` for message boxes trigger `msg` calls with a vector of their space-delimited contents. An example to illustrate

Create a message box with the text "reset", then

```
(defn msg [this inlet ms]
  (when (= ms ["reset"])
    (do
      (reset! n 0)
      (out @n))))
```

Lets specify what to reset to. Connect another message box "reset 234"

```
(defn msg [this inlet [command i]]
  (println command i)
  (when (= command "reset")
    (do
      (reset! n (or i 0))
      (out @n))))
```

## Programmatic layout

You're probably pretty tired of creating all these bangs and message boxes by hand, huh.

Try this

```
(def my-button
  (mnew "button" 200 200))
```

So much easier, right? Well how about this

```
(for [i (range 100)] (mnew "button" (* i 10) 200))
```

Whoa, right. Now go delete them all one-by-one.

Now that you've been through that, it's probably better to assign your creations so you can refer to them later.

```
(def buttons (for [i (range 100)] (mnew "button" (* i 10) 200)))
```

What, whuh? Didn't work. Or did it?

```
(doall buttons)
```

There they are. Clojure `for` is lazy and not evaluated until needed. `doall` forces evaluation (as does printing to the repl, which is why it worked when we didn't assign them to buttons.)

You could also wrap the `for` statement in `doall` before assigning to `buttons`.

Okay, now let's delete them. We use `mremove`, which isn't called `remove` because Clojure already has a `remove` function.

```
(map remove buttons)
```

Pretty neat, huh.

`map` is also lazy, FYI. It works only because the repl realizes the results to print them out. Also, it's not really supposed to be used for side-effecting operations like `mremove`. You should be using `doseq`.

```
(doseq [b buttons] (mremove b))
```

But that's just not as terse and cool as `(map mremove buttons)`, so do what you like.

## Connecting things

Still connecting objects with a mouse. Pity for you.

```
(def button (mnew "button" 200 200))

(connect button 0 box 0)
```

## Cast of characters

Who's this `box` character? It's the the box that encloses `mxj mpls`. In max we connect boxes, which are all subclasses of `MaxBox` in java extensions. (Clojure just wraps the java api. You knew that, right?)

What to know more about `MaxBox`es? You'll want to read the java api.

Mac: /Applications/Max [version]/java-doc/api/index.html
Windows: TODO
Linux: TODO

You also have access to a few others

- mpls [MaxObject] - Our custom java class. Also the first argument to any msg-type function (`msg`, `bang`, `int-msg`)
- box [MaxBox] - The box enclosing `mxj mpls`
- patcher [MaxPatcher] - the patcher 
- window [MaxWindow] - the window

## Misc

In case you care, you can send args when creating `mpls`. One arg defines the nrepl port (default 51580), two args define the number of inlets and outlets you want (you get an extra info outlet, no extra charge!). Three args set `port`, `n-inlets`, `n-outlets`. Weird scheme, isn't it.

```
mxj mpls 1235 5 5
```

## Read the code!

`mpls` is small, small. You can learn about many things that aren't
documented by taking a look at the code.

## Wanna help?

Those TODOs in this README need TODOing. Pull requests or issues or emails could solve that if you're working on Windows or Linux.

Also, I haven't tested this on anything on but my machine. Pull requests for bug or missing features are appreciated. Github issues are okay, too. Twitter shaming, not so much (though I'm not on Facebook, so feel free to post hurtful messages about bugs there).

And most of all, if you do something cool with this, post about it, and let me know.

## License

Copyright © 2013-2015 Selah Ben-Haim
Distributed under the Eclipse Public License, the same as Clojure.

TODO: Remove max object from uberjar
