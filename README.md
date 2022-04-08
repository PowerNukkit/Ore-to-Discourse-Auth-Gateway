# PowerNukkit Ore to Discourse Auth Gateway
This app works as a simple replacement for [SpongeAuth][SpongeAuth] for
those who want to use [Discourse][Discourse] as the main SSO auth provider.

This app does not hold any state and don't store any data, all it does is to convert HTTP requests from 
[Ore][Ore] to [Discourse][Discourse] and vice-versa.

## Setup
### Ore
1. Edit the `Ore` settings file `Ore/ore/conf/ore-default-settings.conf` in the folder where you installed `Ore`
2. Inside the `auth` block set a secure secret at `sso` ➡️ `secret`, this will be your `--auth-sso-api-key`
3. Inside the `auth` ➡ `api` set the url where this app will be running at `url`, this will be your `--base-url`
4. (Optional) Inside the `auth` ➡ `api` set the `avatar-url` to `https://{domain-of-your-discourse-forum}/user_avatar/{domain-of-your-discourse-forum}/%s/120/1.png` replacing `{domain-of-your-discourse-forum}` with the domain of your discourse forum, note that it appears twice in the URL.

### Discourse
1. Go to your `Discourse Admin Panel` ➡️ `Settings` ➡️ `Login`
2. Enable `enable discourse connect provider` (Please don't confuse with `enable discourse connect`, the setting ends with **provider**)
3. Add the host where this app will be running and a random and secure SSO secret key of your choice to the `discourse connect provider secrets` settings, this will be your `--discourse-sso-secret` parameter
4. Go to `Discourse Admin Panel` ➡️ `Customise` ➡️ `Themes` and repeat the next steps for each enabled theme that you have.
5. Click on the theme name, then on the `Edit CSS/HTML`
6. Add this to the end of the `</head>` tab:
    ```html
     <link rel="stylesheet" href="//code.jquery.com/ui/1.13.1/themes/base/jquery-ui.css">
    ```
7. Add this to the end of the `</body>` tab (note: You can customise the content of the `logging-out-dialog` as you wish, the animation colors can be changed, just search for `fill=` inside the `svg` tag)
    ```html
    <div id="logging-out-dialog" title="Please wait..." style="display: none">
    
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" style="margin:auto;background:#fff;display:block;" width="200px" height="200px" viewBox="0 0 100 100" preserveAspectRatio="xMidYMid">
            <g transform="translate(50 50)">  <g transform="translate(-19 -19) scale(0.6)"> <g>
            <animateTransform attributeName="transform" type="rotate" values="0;45" keyTimes="0;1" dur="0.2s" begin="0s" repeatCount="indefinite"></animateTransform><path d="M31.35997276079435 21.46047782418268 L38.431040572659825 28.531545636048154 L28.531545636048154 38.431040572659825 L21.46047782418268 31.359972760794346 A38 38 0 0 1 7.0000000000000036 37.3496987939662 L7.0000000000000036 37.3496987939662 L7.000000000000004 47.3496987939662 L-6.999999999999999 47.3496987939662 L-7 37.3496987939662 A38 38 0 0 1 -21.46047782418268 31.35997276079435 L-21.46047782418268 31.35997276079435 L-28.531545636048154 38.431040572659825 L-38.43104057265982 28.531545636048158 L-31.359972760794346 21.460477824182682 A38 38 0 0 1 -37.3496987939662 7.000000000000007 L-37.3496987939662 7.000000000000007 L-47.3496987939662 7.000000000000008 L-47.3496987939662 -6.9999999999999964 L-37.3496987939662 -6.999999999999997 A38 38 0 0 1 -31.35997276079435 -21.460477824182675 L-31.35997276079435 -21.460477824182675 L-38.431040572659825 -28.531545636048147 L-28.53154563604818 -38.4310405726598 L-21.4604778241827 -31.35997276079433 A38 38 0 0 1 -6.999999999999992 -37.3496987939662 L-6.999999999999992 -37.3496987939662 L-6.999999999999994 -47.3496987939662 L6.999999999999977 -47.3496987939662 L6.999999999999979 -37.3496987939662 A38 38 0 0 1 21.460477824182686 -31.359972760794342 L21.460477824182686 -31.359972760794342 L28.531545636048158 -38.43104057265982 L38.4310405726598 -28.53154563604818 L31.35997276079433 -21.4604778241827 A38 38 0 0 1 37.3496987939662 -6.999999999999995 L37.3496987939662 -6.999999999999995 L47.3496987939662 -6.999999999999997 L47.349698793966205 6.999999999999973 L37.349698793966205 6.999999999999976 A38 38 0 0 1 31.359972760794346 21.460477824182682 M0 -23A23 23 0 1 0 0 23 A23 23 0 1 0 0 -23" fill="#1babfa"></path></g></g> <g transform="translate(19 19) scale(0.6)"> <g>
            <animateTransform attributeName="transform" type="rotate" values="45;0" keyTimes="0;1" dur="0.2s" begin="-0.1s" repeatCount="indefinite"></animateTransform><path d="M-31.35997276079435 -21.460477824182675 L-38.431040572659825 -28.531545636048147 L-28.53154563604818 -38.4310405726598 L-21.4604778241827 -31.35997276079433 A38 38 0 0 1 -6.999999999999992 -37.3496987939662 L-6.999999999999992 -37.3496987939662 L-6.999999999999994 -47.3496987939662 L6.999999999999977 -47.3496987939662 L6.999999999999979 -37.3496987939662 A38 38 0 0 1 21.460477824182686 -31.359972760794342 L21.460477824182686 -31.359972760794342 L28.531545636048158 -38.43104057265982 L38.4310405726598 -28.53154563604818 L31.35997276079433 -21.4604778241827 A38 38 0 0 1 37.3496987939662 -6.999999999999995 L37.3496987939662 -6.999999999999995 L47.3496987939662 -6.999999999999997 L47.349698793966205 6.999999999999973 L37.349698793966205 6.999999999999976 A38 38 0 0 1 31.359972760794346 21.460477824182682 L31.359972760794346 21.460477824182682 L38.431040572659825 28.531545636048154 L28.531545636048183 38.4310405726598 L21.460477824182703 31.35997276079433 A38 38 0 0 1 6.9999999999999964 37.3496987939662 L6.9999999999999964 37.3496987939662 L6.999999999999995 47.3496987939662 L-7.000000000000009 47.3496987939662 L-7.000000000000007 37.3496987939662 A38 38 0 0 1 -21.46047782418263 31.359972760794385 L-21.46047782418263 31.359972760794385 L-28.531545636048094 38.43104057265987 L-38.431040572659796 28.531545636048186 L-31.359972760794328 21.460477824182703 A38 38 0 0 1 -37.34969879396619 7.000000000000032 L-37.34969879396619 7.000000000000032 L-47.34969879396619 7.0000000000000355 L-47.3496987939662 -7.000000000000002 L-37.3496987939662 -7.000000000000005 A38 38 0 0 1 -31.359972760794346 -21.460477824182682 M0 -23A23 23 0 1 0 0 23 A23 23 0 1 0 0 -23" fill="#ac384d"></path></g></g></g>
        </svg>
        <p align="center">
            Logging out...
        </p>
    
    
    </div>
    
    <script src="https://code.jquery.com/ui/1.13.1/jquery-ui.js"></script>
    <script>
        $( "#logging-out-dialog" ).dialog({
            autoOpen: false,
            modal: true,
            closeOnEscape:false,
            open: function(event, ui) {
                $(".ui-dialog-titlebar-close", $(this).parent()).hide();
            }
        });
        if(window.location.hash=='#action--ore-logout'){
            $(function(){
                $( "#logging-out-dialog" ).dialog( "open" );
                let user = $("#current-user > a").attr("title");
                $.ajax({
                    url: '/session/' + user,
                    type: 'DELETE',
                    success: function(result) {
                        const params = new Proxy(new URLSearchParams(window.location.search), {
                          get: (searchParams, prop) => searchParams.get(prop),
                        });
                        let referer = params.referer;
                        if (!referer) {
                            // Change to your fallback url
                            referer = 'https://your.ore.domain.example.com/';
                        }
                        document.location = referer;
                    }
                });
            });
        }
    </script>
   ```

### This App
Now you just need to run this app, you can build a single jar, build a dist-zip, or just use `gradlew run`,
I'm going to use `gradlew run` here because it's easier.

1. Download this repository
2. Open the folder with your favorite terminal (PowerShell, CMD, Konsole, gnome-terminal, xTerm, etc)
3. Run `./gradlew run --args="-h"` to learn your options, you can also set the parameters using environment, for example you can set an environment parameter named `DISCOURSE_URL` instead of passing it as `--discourse-url` or `-du` directly.
4. Call again but instead of `-h` fill the arguments with real data, and you will be good to go after you get a log saying `Application started`. 

# License
This software is provided without any warranty.

[Ore]: https://github.com/SpongePowered/Ore
[SpongeAuth]: https://github.com/SpongePowered/SpongeAuth
[Discourse]: https://github.com/discourse/discourse#readme
