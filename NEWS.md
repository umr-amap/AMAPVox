# Changelog

## AMAPVox 2.5

- 2.5.0 (yyy-MM-dd) (_Released/Not release on CRAN_)
  - Feature:
  - Bugfix: 

## AMAPVox 2.4

- 2.4.2 (2026-02-16) (_Released on CRAN_)
  - Bugfix: add `offline` option in R package AMAPVox::run function [#73](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/73)
  - Bugfix: grant native access to javafx/graphics [#74](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/74)
- 2.4.1 (2025-11-17) (_Not released on CRAN_)
  - Bugfix: LAS shot construction no longer hangs when a LAS point’s GPS time matches the trajectory’s max time. [#71](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/71) & [#72](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/72). Thanks to Daniel K. for raising it!
- 2.4.0 (2025-07-16) (_Not released on CRAN_)
  - New feature: support LAS extra-bytes variables for relative echo weight [#63](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/63)

## AMAPVox 2.3

- 2.3.3 (2025-01-21) (_Not released on CRAN_)
  - Bugfix: updated laszip4j library to read compressed extra-bytes data [#55](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/55)
  - Bugfix: default LAS time range max value set to integer zero [#56](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/56)
- 2.3.2 (2024-12-03) (_Not released on CRAN_)
  - Bugfix: default lead area set to 50cm2 #53
  - Bugfix: save dialog intial directory fail-safe #54
  - New feature: added Trimble x7 laser specification #12
- 2.3.1 (2024-09-11) (_Not released on CRAN_)
  - Bugfix: open and save dialog fail-safe [#51](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/51)
- 2.3.0 (2024-09-09) (_Not released on CRAN_)
  - New feature: trajectory time span [#41](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/41)
  - New feature: optional normalization for relative echo weight [#50](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/50)
  - New feature: user-defined Plant Area Density variable and Leaf Angle Distribution in canopy tools [#40](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/40), [#49](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/49)
  - New feature: AMAPVox::plot3d axis can either be i, j, k or x, y, z [#47](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/47)
  - Bugfix: removed differential reflectance between ground and vegation [#38](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/38)
  - Bugfix: LAS points outside trajectory time span automatically discarded [#45](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/45)
  - Bugfix: SOP, POP & VOP transformation matrix were always enabled [#42](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/42)
  - Bugfix: erroneous Zmax value in voxel boundary quick search [#44](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/44)

## AMAPVox 2.2

- 2.2.1 (2024-03-17) (_Released on CRAN_)
  - Aligned R package version onto Java core version
  - Updated binary packages URL (#31, #32)
  - Bugfix in transmittance map tool (#37)
  - Bugfix in XYB point cloud reader (#30)

- 2.2.0 (2024-02-05) (_Not released on CRAN_)
  - LAS/LAZ library replaced with LASzip4J (#24)
  - Added "Recent files" menu (#23)
  - Bugfix save as function (#19)
  - Handled NaN values in DTM ascii files (#20)
  - Added persistence in preferences 
  - Transformation matrix and point cloud filters now trigger changes for save button (#17)
  
## AMAPVox 2.1
  
**Note:** This release includes only Java binaries (no R package). However, these binaries remain accessible and can be installed using the **version manager** in the current AMAPVox R package.

- 2.1.2 (2023-01-18)
  - Handled case when number of echo ranks is greater than number of lines in echo weight matrix (#21)

- 2.1.1 (2023-01-12)
  - Added validation support to echo weight matrix

- 2.1.0 (2023-12-21)
  - Included several echo weigh options (rank echo weight, relative echo weight, strongest echo weight) (#26, #27)
  - Updated Java & JavaFX version to 21
  - Updated JOGL/GLUEGEN libraries to 2.5.0
  - Updated maven plugins and external libraries to latest version
  
## AMAPVox 2.0
  
**Note:** This release includes only Java binaries (no R package). However, these binaries remain accessible and can be installed using the **version manager** in the current AMAPVox R package.

- 2.0.2 (2023-07-03)
  - Bugfix in PTG scan reading function, that should solve #3 (though unrelated at first sight)
  - LASzip Java API now reads LAS variables user_data and point_source_ID

- 2.0.1 (2023-06-23)
  - Missing shots were not reconstructed for PTX/PTG scans since 1.7.0.
  - Hemispherical photograph tool now accepts custom PAD variable.
  - Enabled echo attribute filtering for RXP scans (based on reflectance, intensity or deviation values).

- 2.0.0 (2023-03-16)
  - Updated source code to **Java 11** standard and refactored it into **Java modules**.
  - Created binaries for both Windows and Linux OS with `jlink` and `jpackage` tools. 

## AMAPVox 1.0

- 1.0.1 (2023-06-23) (_Released on CRAN_)
  - Fixed issue #5, unzip failed on Windows and AMAPVox did not run.
  - Deleted Java as System Requirement in DESCRIPTION (optional since AMAPVox binary 2.0)

- 1.0.0 (2023-03-15) (_Released on CRAN_)
  - From binary AMAPVox 2.0, no need to install local Java VM, it is embedded within AMAPVox

## AMAPVox 0.13

- 0.13.0 (2023-03-16) (_Not released on CRAN_)
  - Get AMAPVox binaries from forge.ird.fr with Gitlab API

- 0.12.1 (2022-12-05) (_Released on CRAN_)
  - Fixed #2 Misuse of `data.table.merge` in `plantAreaDensity` function

- 0.12.0 (2022-08-24) (_Released on CRAN_)
  - Added new function `run` that either runs AMAPVox in batch mode or launches GUI
  - Deleted gui/run check.update option, assuming that version set to _latest_ implies to check for updates

- 0.11.0 (2022-08-24)
  - Function `readVoxelSpace` can read zipped voxel file.
  - Function `plot` accepts voxel subset in y parameter.
  - Function `plantAreaDensity` returns separate data table and accepts voxel subset.

- 0.10.1 (2022-08-24) (_Not released on CRAN_)
  - Bugfix in `toRaster` function.
  - Bugfix across several functions (`canopyHeight`, `groundElevation`, `plantAreaIndex`) for merging/joinging data.table based on `on` instead of `key`.
  - Bugfix in `computeG` function that speed up calculation for large theta vector.

- 0.10.0 (2022-04-20) (_Released on CRAN_)
  - Added new functions `plantAreaDensity`, `plantAreaIndex`.
  - Added new function `toRaster`.

- 0.9.0 (2022-03-30) (_Not released on CRAN_)
  - Added new functions `ground`, `aboveGround`, `belowGround`, `groundEnergy`, `groundElevation`.
  - Added new functions `canopy`, `aboveCanopy`, `belowCanopy`, `canopyHeight`.
  - Renamed `removeButterfly` into `butterfly` function.

- 0.8.0 (2022-03-24) (_Not released on CRAN_)
  - Added new function `plantAreaDensity` to compute plant area density.
  - Added new function `fillNA` to fill missing values in voxel space.
  - Added new function `crop` to crop a voxel space to a specified cuboid.

- 0.7.0 (2022-03-04) (_Not released on CRAN_)
  - Added new function `removeButterfly` to remove non-empty isolated voxel in voxel space.

- 0.6.0 (2022-02-23) (_Not released on CRAN_)
  - Bug fix in `writeVoxelSpace` function.

- 0.5.0 (2021-10-19) (_Not released on CRAN_)
  - Normalized local paths.
  - RGL library not imported, suggested only.

- 0.4.0 (2021-10-19) (_Not released on CRAN_)
  - Bug fix in the version manager.
  - Added first draft of vignettes.

- 0.3.0 (2021-09-03) (_Not released on CRAN_)
  - Added `AMAPVox::gui` function to run AMAPVox with Graphical User Interface.

- 0.2.0 (2021-07-13) (_Not released on CRAN_)
  - Added 3d `plot` function for voxel space.

- 0.1.0 (2021-07-09) (_Released on CRAN_)
  - First public release
