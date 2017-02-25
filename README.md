This repository is derived from:

https://github.com/JonathanLink/PDFLayoutTextStripper

It extracts the text from a pdf while keeping its visual organisation.

Contrary to PDFLayoutTextStripper, this repo includes a maven configuration.
To use it: first copy your pdf file to ./todo.pdf and then run

    mvn test

In case extraction fails because of PDF protection, you should first
remove the protection from your pdf with:

    qpdf --decrypt infile outfile

