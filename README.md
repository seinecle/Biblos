Biblos
======

Utilities to map a network of co-authors (from a bibTex file) with Circos.

Work in progress.
At the moment these utilities focus on a special case:
- the bibTex file comprises the references from a single author
- the utilities create a circos map of all the co-authors of this single author.
 
These utilities generate a circos.conf file, a karyotype and a \<links\> file.

Features:
- each author is given one of 7 colors.
- possibility to give a range of year as a parameter (only the bibliographical references comprised in these years will be parsed)


Example of output:
http://clementlevallois.net/circos/vdb/<br>
http://clementlevallois.net/circos/circos.svg