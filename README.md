<!--
**elf4j/elf4j** is a ✨ _special_ ✨ repository because its `README.md` (this file) appears on your GitHub profile.

Here are some ideas to get you started:

- 🔭 I’m currently working on ...
- 🌱 I’m currently learning ...
- 👯 I’m looking to collaborate on ...
- 🤔 I’m looking for help with ...
- 💬 Ask me about ...
- 📫 How to reach me: ...
- 😄 Pronouns: ...
- ⚡ Fun fact: ...
-->

# Easy Logging Facade for Java (ELF4J)

A no-fluff Java logging facade. 

Client applications can switch logging frameworks at deployment time, no code change needed. There have been similar efforts such as Apache Commons Logging and SLF4J. If you have used some of those over the years, you may understand why attempts like this still exist.

Hopefully, the [`Logger`](https://github.com/elf4j/elf4j-api/blob/main/README.md#the-logger) API is boringly simple and easy to use. The out-of-the-box logging behavior is NO-OP, intentionally, but some working logging providers are available: [LOG4J](https://github.com/elf4j/elf4j-log4j), [tinylog](https://github.com/elf4j/elf4j-tinylog), [LOGBACK](https://github.com/elf4j/elf4j-logback), ... 

The [Java Service Provider Interfaces (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism is employed so the client application can elect or change the in-effect logging framework, at deployment time, without code change.
