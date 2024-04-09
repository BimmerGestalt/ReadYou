<div align="center">
    <img width="200" height="200" style="display: block; border: 1px solid #f5f5f5; border-radius: 9999px;" src="https://raw.githubusercontent.com/Ashinch/ReadYou/main/fastlane/metadata/android/en-US/images/icon.png">
</div>

<br>
<br>
<br>

<div align="center">
    <img alt="GitHub" src="https://img.shields.io/github/license/BimmerGestalt/ReadYou?color=c3e7ff&style=flat-square">
    <a target="_blank" href="https://github.com/Ashinch/ReadYou">
        <img alt="Upstream" src="https://img.shields.io/badge/upstream-ReadYou-c3e7ff?&style=flat-square">
    </a>
</div>

<div align="center">
    <h1>Read You</h1>
    <p>An Android RSS reader presented in <a target="_blank" href="https://m3.material.io/">Material You</a> style, with BMW/Mini app support.</p>
    <p>English&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README-de.md">Deutsch</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README-zh-CN.md">简体中文</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README-zh-TW.md">繁體中文 (Outdated)</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/Ashinch/ReadYou/blob/main/README-fa.md">فارسی (Outdated)</a></p>
    <br/>
    <br/>
    <img src="https://raw.githubusercontent.com/Ashinch/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/startup.png" width="19.2%" alt="startup" />
    <img src="https://raw.githubusercontent.com/Ashinch/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/feeds.png" width="19.2%" alt="feeds" />
    <img src="https://raw.githubusercontent.com/Ashinch/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/flow.png" width="19.2%" alt="flow" />
    <img src="https://raw.githubusercontent.com/Ashinch/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/read.png" width="19.2%" alt="read" />
    <img src="https://raw.githubusercontent.com/Ashinch/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/settings.png" width="19.2%" alt="settings" />
    <br/>
    <br/>
</div>

## Features

**Read You** is an Android RSS reader presented in [Material You](https://m3.material.io/) style.

The following are the progress made so far and the goals to be worked on in the near future:

- [x] Subscribe to RSS links
- [x] Import or export OPML files
- [x] Notification of new articles
- [x] Article readability optimization
- [x] Full content parse for original articles
- [x] Multi-account
- [ ] Bionic reading
- [ ] Read aloud
- [ ] Android widget
- [ ] ...

## Integration

**Read You** integrates with some of third-party service APIs to support you in using your existing cloud accounts as data sources.

- [x] Fever
- [x] Google Reader
- [x] FreshRSS
- [ ] Miniflux
- [ ] Tiny Tiny RSS
- [ ] Inoreader
- [ ] Feedly
- [ ] Feedbin
- [ ] ...

If an [AAIdrive](https://github.com/BimmerGestalt/AAIdrive)-compatible car is connected, then ReadYou will be added as a News icon to the ConnectedDrive interface in the car.

## Download

[<img src="https://s1.ax1x.com/2023/01/12/pSu1a36.png" alt="Get it on GitHub" height="80">](https://github.com/BimmerGestalt/ReadYou/releases)

Please use the [official ReadYou app](https://github.com/Ashinch/ReadYou) if you don't need car features because it will be more updated.

## Sponsor

**Read You** is a free open source software that benefits from the open source community and every user can enjoy it's full functionality for free, so if you appreciate my current work, you can buy me a cup of coffee ☕️.

<a href="https://www.buymeacoffee.com/ashinch"><img width="200px" src="https://img.buymeacoffee.com/button-api/?text=Buy me a coffee&emoji=&slug=ashinch&button_colour=FFDD00&font_colour=000000&font_family=Cookie&outline_colour=000000&coffee_colour=ffffff" /></a>

Thanks for all the love and support ❤️

## Localization

Thank you to each of the **Read You** translators, and if you would like to contribute, please submit a translation via [Weblate](https://hosted.weblate.org/engage/readyou/).

[<img src="https://hosted.weblate.org/widgets/readyou/-/horizontal-auto.svg" alt="" />](https://hosted.weblate.org/engage/readyou/)

## Build

> Welcome to open a [pull request](https://github.com/Ashinch/ReadYou/pulls). [GitHub Actions](https://github.com/Ashinch/ReadYou/actions) automatically packages all flavors of apk files for each commit.

**Read You** is based on [Jetpack Compose](https://developer.android.com/jetpack/compose) toolkit for building Android's native UI.

1. First you need to get the source code of **Read You**.

   ```shell
   git clone https://github.com/Ashinch/ReadYou.git
   ```

2. Then open it via [Android Studio (latest version)](https://developer.android.com/studio).

3. When you click the `▶ Run` button, it will be built and run automatically.

    > In case of lag, please select Release version build.

## Credits

### Open Source Projects

- [MusicYou](https://github.com/Kyant0/MusicYou)
- [ParseRSS](https://github.com/muhrifqii/ParseRSS)
- [Readability4J](https://github.com/dankito/Readability4J)
- [opml-parser](https://github.com/mdewilde/opml-parser)
- [compose-html](https://github.com/ireward/compose-html)
- [Rome](https://github.com/rometools/rome)
- [Feeder](https://gitlab.com/spacecowboy/Feeder)
- [Seal](https://github.com/JunkFood02/Seal)
- [news-flash](https://gitlab.com/news-flash)
- [besticon](https://github.com/mat/besticon)
- ...

### Special Thanks

[<img src="https://avatars.githubusercontent.com/u/76829190?v=4" width="180" height="180" style="display: block; border: 1px solid #f5f5f5; border-radius: 9999px;"/>](https://github.com/Kyant0)

Thanks to **@Kyant0** for the design inspiration and Monet engine implementation for **Read You**.

[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="200" alt="Copyright © 2000-2023 JetBrains s.r.o. JetBrains and the JetBrains logo are registered trademarks of JetBrains s.r.o."/>](https://www.jetbrains.com/)

Thanks to **JetBrains** for allocating free open-source licences for IDEs for **Read You**.

[<img src="https://hosted.weblate.org/widgets/readyou/-/287x66-white.png"  width="200"/>](https://hosted.weblate.org/engage/readyou/)

Thanks to **Weblate** for providing free hosting of open source projects for **Read You**.

## License

GNU GPL v3.0 © [Read You](https://github.com/Ashinch/ReadYou/blob/main/LICENSE)
