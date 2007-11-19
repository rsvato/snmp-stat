package net.paguo.statistics.snmp.commands;

/**
 * @author Reyentenko
 */
public interface RenameStrategy {
    String renameInterface(String ifaceName, Long ifaceId);
}
