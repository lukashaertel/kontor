This project houses [[Kontor]], [[Rome]] and [[Wepwawet]].


### Building
The project should build out of the box. To run properly in IntelliJ, edit the run configuration of the respective main and replace the Kotlin build with a call to the gradle task build. This is neccessary since this project utilizes the serialization compiler plugin.

![IntelliJ run configuration](https://github.com/lukashaertel/kontor/raw/wsdata/config.png)

## Kontor
A network server and client library supporting arbitrary message types and generalized inbound/outbound channels. This project demonstrates some experimental Kotlin features, namely [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) and the [Serialization Prototype](https://github.com/elizarov/KotlinSerializationPrototypePlayground).

## Rome
Rome provides a simple to implement *do* and *undo* timeline. *do*s may return a carry-over state that can be used by *undo* to revert the action. The repository supports dropping unneeded actions and setting a *soft upper boundary* to view previous states and prevent execution of actions in the "future".

## Wepwawet
Wepwawet uses change tracking to generalize *undo* operations. They are defined using an embedded DSL. The ultimate goal is to make an out-of-the-box synchronization solution for complex multiplayer games.
