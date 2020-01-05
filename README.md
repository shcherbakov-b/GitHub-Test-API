**Hello**

This is a simple application to work with GitHub API

#How to

To run the application just open console and print **sbt run**(Requires installed sbt :D)
Then go to **localhost:8080/org/{org_name}/contributors** (Note: paste your organization here => **{org_name}**)

Do not forget to set environment variable **GH_TOKEN**

Application was built with **http4s** and **cats-effects**
Why these libs? - Because I didn't have a deep knowledge of either **http4s** or **cats-effects**, so I wanted to try :)

#TO DO

*Add logger*

*Write tests*

*Caching*

*Proper documentation*