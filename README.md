# device-concord4
Concord 4 Hubitat Device Hander and Companion Door Watcher App

This is a project for connecting the Concord 4 alarm system to Hubitat.  You will need a computer/rasperry pi/etc with a USB serial cable connected to the superbus 2000 automation module.

Please be aware that the concord server will communicate to your Concord 4 Panel as if it's a keyfob. This means that no security code is necessary to disarm the system. Hubitat does have features to lockdown dashboard with PIN if you are not comfortable with this level of access.

## Prerequisites

 - Hardware (Concord 4 or equivalent panel) with a Superbus 2000 automation module attached to it
 - RS232 connection (to the AM panel)
 - Python 2.7
 - Python packages: requests, future, pyserial (pip install)
 - Raspberry Pi (recommended)

## Installation

These instruction assume you have <a href="https://docs.hubitat.com/index.php?title=Hubitat_Safety_Monito">Hubitat Security Monitor</a> set up already. 

 1. Download all files from this repository
 2. Navigate to your Hubitat home page
 3. Click **Drivers Code**
 4. Click **+New Driver**
 5. Open *Concord4_driver.groovy* found in the *hubitat concord 4 driver* folder of this repo. You can use your favorite text editor.
 6. Copy and paste all code to the New Driver screen on Hubitat.
 7. Click **Save**
 8. Perform a similiar process for the app code. Open the "Apps Code" page on Hubitat
 9. Click **+New App**
 10. Open "Concord_4_Door_Watcher_app.groovy" found in the *hubitat concord 4 door watcher app* folder of this repo. You can again use your favorite text editor.
 11. Copy and paste all code to the New App screen on Hubitat.
 12. Click **Save**
 13. Navigate to the Devices page on your Hubitat. 
 14. Click the **+Add Device** button.
 15. Choose a Virtual Device
 16. Give your device a name and select the Concord4 driver in the *Type* dropdown list. Click **Save**.
 17. Navigate to the Concord4 device you just created.
 18. Scroll down to the preferences.
 19. Fill in the IP address of your Concord 4 Server (we haven't set it up yet, but you should know the IP address to your raspberry pi). Keep the port number as-is. 
 20. You'll need to sync some data with the .conf file that you will be using on the raspberry pi. Go to the *concordsvr* folder of this repo and open the *concordsvr.conf* file.
 21. In both the .conf file and the device prefernces page, enter a password (make one up... they just need to be the same in both places), the Maker API App ID, and Maker API Authentication Token. The .conf file has instruction on how to get the App ID and token.
 22. You'll need to know the setup of your Concord 4 for this step. It has been setup up that certain contacts are in certain "zones". You may have to play around to figure out which zone is which. If you know what Zones 1-4 are, then give those a name in the device preferences page, but leave any unused zone names blank.
 23. Click **Save Device**. 
 24. Since we already have the .conf file open, let's fill out the remaining two field that we need to touch. Enter your hub's IP address as well as the device ID. The .conf file has instructions for both. Make sure you also save your .conf file.
 25. Navigate to the Apps page on your Hubitat. 
 26. Click the **+Add User App** button.
 27. Choose Concord 4 Door Watcher
 28. Open the Concord 4 Door Watcher app. Select your Concord 4 device. This app allows you to add an additional door so that you can't Arm Home without that door being closed too. For me, my exterior doors are all monitored by the Concord 4 but I wanted to keep from Arming Home if my Garage Door was open. Select the doors are already monitored by the Concord 4 and whatever other doors you want. This only keeps the system from arming if those doors are closed. It does not allow the alarm to monitor those doors. Side note: This driver couples with Hubitat Security Monitor. You want to ensure you have selected the same doors in Arm Home mode for both HSM and Concord 4 Door Watcher app.
 29. Click done and exit the app.
 30. Setting up your dashboard is outside of the scope of these instructions, but it is good to point out that the driver sets up four virtual switches: Disarm, Arm Home, Arm Away, and Silence. Silence works by itself and when it's on, the Concord 4 will beep on state changes. The other three switches are tied together in the driver code such that they work like buttons. They _should_ mirror the state of the Concord 4 device at all times. I also use the Concord 4 device with the security keypad template to show the status.
 31. Login to your Pi and install python and the packages (see prerequisites) via pip (if not already installed). Note that default raspbian comes with it, as does NOOBS
 32. Copy the entire *concordsvr* into a directory you can access, such as *~/*. You can use *git clone* as an easy way to get it from the repository.
 33. Replace *concordsvr.conf* with the one we edited. Alternatively, edit *concordsvr.conf* with your favourite editor, such as *nano concordsvr.conf*
 34.  Start the program using *python concordsvr.py*
 35. You'll also need to handle restarting the script at any reboot. I used supervisord, but you can use whatever you're comfortable with. This is beyond the scope of these instructions.
 36. If all went well, the Hubitat side will automatically populate info the python server needs to communicate to individual children devices. If this doesn't work, you can use the **Send Concord Config** command which is listed on the security panel's parent device page of Hubitat.
 37. Use **tail -n 100 <path to your concordsvr folder>/concordsvr.log** to check the log and see everything is working right. It's also good to be standing in front of the panel while you test it out.


## API
The proxy accepts the following REST requests to control the alarm system.

* /concord/refresh/
* /concord/arm/stay/[loud]
* /concord/arm/away/[loud]
* /concord/disarm/[loud]
* /concord/keypress/[key]
