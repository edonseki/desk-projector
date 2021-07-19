# desk-projector
A simple Desktop Application with Java and Java Swing that allows Screen Sharing between team members on an internal network


### What for a problem does this tool solve?
Sometimes, you need to share your screen on a desk without creating an external meeting or asking your team members to come and look at your screen. In such cases, sharing the screen internally without going out on the internet helps a lot. That was the problem I had in some situations where I couldn't book a meeting room with a TV where I could share my screen.

### How to use?
This tool is a Desktop Application that works at least with Java 1.8. You can build the software on your own as a Java Swing Application or download the release from GitHub.

Open the Jar file on your PC and a simple window will appear. The Projector Key is the key you have to share with your team member to see your Screen and using the Join functionality you're able to access other's screens.

### Your local network
This tool works on local network and in order to make sure you and your colleagues are in the same network you should make sure the third octet of your v4 IP is the same. Example: 0.165 and 0.128. 

### Build projector
1. Run <code>mvn install</code> on main path
2. After the successful build run/share the file from target folder with <code>jar-with-dependencies</code> and you're ready to have local screen sharing.

### Only for Mac Users
In case you face any issue with screen sharing permission, then tou could reset entirely the permissions for screen sharing using the command below:
<code>tccutil reset ScreenCapture</code>.


___
<a href="https://www.buymeacoffee.com/edonseki" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>