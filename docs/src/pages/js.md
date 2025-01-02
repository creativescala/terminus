# Javascript Backend

The Javascript backend uses [xterm.js], which allows terminal applications to be embedded in websites. It's what is used for all the examples in this documentation. Running in the web browser means there are some differences from standard terminal applications, but the majority of the Terminus interface stays the same.


## Running a Terminal

When running a `Terminal` you need to provide the ID of a DOM element where you want the terminal mounted. For example, if you have the following within a web page

```html
<div id="terminal"></div>
```

you would construct a `Terminal` with the following.

```scala
Terminal.run("id"){ 
  // Terminal code here
}
```

There are optional arguments to specify the number of rows and columns the terminal occupies.

```scala
Terminal.run("id", rows = 24, cols = 80){ 
  // Terminal code here
}
```


## User Input

User input is asynchronous in the web browser, so the normal @:api(terminus.Reader) interface doesn't work. Instead, Terminus provides a `readKey` method, which returns a `Future[String]`, where the `String` is the [key from a DOM `KeyboardEvent`](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key).

[xterm.js]: https://xtermjs.org
