## Changelog of KML Driver

- 1.0.0 (2021-04-13) migration to OpenJUMP 2
- 0.2.5 (2019-02-17) also read <placemark> if no <Folder> exists
- 0.2.4 (2015-04-05) reenable loading of kmz files, reformatting removing tabs
  - move version variable to build.xml header
  - move changes into this dedicated file
  - create resources/kml/ProjectStringsList.pjl which is needed
  - UTM reprojection, add it to ant targets
- 0.2.3 (2015-03-22) fix a regression introduced in 4215 with a change in core
  - OpenJUMP (making xml based drivers charset aware)
- 0.2.2 (2014-12-21) make kml parser charset aware
- 0.2.1 (2014-12-20) fix encoding problem (cf #383)
- 0.2   (2014) version included in OpenJUMP 1.7 and 1.8 PLUS
- 0.1   (2011-09-17)   : first version taken from SkyJUMP source