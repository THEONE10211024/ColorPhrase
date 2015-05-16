# ColorPhrase
Phrase is an Android string resource color setting library 

## Usage
```java
CharSequence formatted = ColorPhrase.from("I'm {Chinese}, I love {China}")
  .withSeparator("{}")
  .innerColor(0xFFE6454A)
  .outerColor(0xFF666666)
  .format();
```
## Preview
![sample](https://github.com/THEONE10211024/ColorPhrase/blob/master/screenshot/Screenshot_2015-05-16-18-12-23.jpeg)
##Download
1)download the project
2)find ColorPhrase.java in "\src\com\medusa\lib" and copy it into your project
3)then you can use it!
4)ps: you should include the android-support-v4.jar in your porject
