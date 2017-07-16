0.2.3
==========
Improvement in parsing performance for quoted data 
Bugfixes
* Additional fix for issue quotes in the end of the line, thanks to https://github.com/jasonk000

0.2.2
==========
Converted tabs to spaces in source files
Bugfixes
* Fix for issue #3 with quotes in the end of the line

0.2.1
==========
Bugfixes
 * Fix NPE occuring when first column is included into parsing results using header in the source API.
   Issue was reported by Pradeep Jaligama.

0.2.0
==========
New features
* header aware parsing
* charset support
* more flexible input config
* performance improvements: less garbage, better composite slice impl., int and long parsers
* new interface with mapper

Bugfixes
* issue with skipping records via stream api
* stream completion flag as returned by advance() was not properly calculated  

0.1.1
==========
* sample project added

0.1.0
==========
* initial release
