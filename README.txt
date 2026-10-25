PUNCH SNOWBALL — Paper 1.21.11

The ZIP contains Java source, not a precompiled JAR.

Build without installing Java:
1. Create a GitHub repository.
2. Upload the CONTENTS of this folder, including .github/workflows/build.yml.
3. Open Actions > Build plugin > Run workflow.
4. After it finishes, download the PunchSnowball-jar artifact.
5. Unzip the artifact and place the .jar in your Paper server plugins folder.
6. Restart the server.
7. As OP, run /givesnowball or /givesnowball <player>.

Features: one reusable snowball; 5-second cooldown; player knockback;
does not modify ordinary snowballs; multiplayer compatible.
Knockback is Punch III-style, not an exact vanilla Punch III calculation.
The cooldown applies to the snowball material, so normal snowballs also
cannot be thrown during the 5-second cooldown by that player.
