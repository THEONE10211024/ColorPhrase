# ColorPhrase
Phrase is an Android string resource color setting library 

## Usage
```java
CharSequence formatted = Phrase.from("I'm {Chinese}, I love {China}")
  .withSeparator("{}")
  .innerColor(0xFFE6454A)
  .outerColor(0xFF666666)
  .format();
```
## Preview
![sample](https://github.com/THEONE10211024/ColorPhrase/blob/master/screenshot/Screenshot_2015-05-16-18-12-23.jpeg)
