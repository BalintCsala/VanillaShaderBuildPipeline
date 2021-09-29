# Vanilla shader build pipeline

This program intends to add support for some of the missing preprocessor options in the vanilla shader pipeline.
So far the following features are implemented

 - Custom include directives 
 - Separate shaders folder from the rest of the resourcepack

## Usage

### Initial setup

To use this program, instead of putting the shaders folder in its default location (`<resourcepack name>/assets/minecraft/`), put it in the root folder (`<resourcepack name>/`). The root folder must also not be located in the resourcepack folder of the game.

### Usage

The program supports two modes of operation

`build <directory>` 

take the files in the specified directory, build them into a working resourcepack shader and put them in the resourcepack folder under the same name (e.g. if the folder the resourcepack is in is called `test`, then this will create a folder named `test` in `.minecraft/resourcepacks/`).
 
`watch <directory> [--exclude/-e <regex> [--exclude/-e <regex>...]]`

Take the files in the specified directory and build them, then continuously keep an eye on changes and update the shader on each. You can set exclusions using the --exclude (-e) command line parameter. This uses regexes, so for instance if you want to ignore every file ending in `.txt`, then use the regex `.*\.txt`.

### Features in detail

#### Custom include directive

When the preprocessor encounters the following line:

```glsl
#vsbp_import<path>
```

It will import the file at the specified path into the shader. The path is relative to the shaders folder inside the root directory. 

The `#` symbol must be the first character of the line, but the pragma is otherwise space-insensitive. You can put comments after the pragma. Examples for good and bad directives:

```glsl
// Good
#vsbp_import<path>                  
#    vsbp_import    <     path     >
#vsbp_import<path> // Import path
// Technically this works, but the result would be placed inside the comment. Very likely to break, so please avoid
/*
#vsbp_import<path>
*/

// Bad
// Space before hashtag
 #vsbp_import<path>
// Space in path name
#vsbp_import<path/my directory/shader.glsl>


```

The include is not limited to any file extension, you can import anything from .glsl files to .txt files.

##### Example:

If my source files look like the following:
```
my-resource-pack:
    > assets ...
    > shaders
        > utils
            > random.glsl
        > test.glsl 
```

And the contents of the files are:

random.glsl
```glsl
int getRandomNumber() {
    return 4; // chosen by a fair dice roll.
              // guaranteed to be random.
}
```

test.glsl
```glsl
#vsbp_import<utils/random.glsl>

int calculateRandomOffset() {
    return getRandomNumber() * 10;
}
```

Then the processed version of test.glsl will be the following:
```glsl
int getRandomNumber() {
    return 4; // chosen by a fair dice roll.
              // guaranteed to be random.
}

int calculateRandomOffset() {
    return getRandomNumber() * 10;
}
```