ALTER TABLE `test1`.`mattsinfo` DROP FOREIGN KEY `fk_mattsinfo_person_id`;
ALTER TABLE `test1`.`mattsinfo` ADD CONSTRAINT `fk_mattsinfo_person_id` FOREIGN KEY (`person_id`) REFERENCES `test1`.`persons` (`person_id`) ON DELETE CASCADE;
ALTER TABLE `test1`.`mattbusyslots` DROP FOREIGN KEY `fk_mattsinfo_mattbusyslots`;
ALTER TABLE `test1`.`mattbusyslots` ADD CONSTRAINT `fk_mattsinfo_mattbusyslots` FOREIGN KEY (`matt_id`) REFERENCES `test1`.`mattsinfo` (`matt_id`) ON DELETE CASCADE;
ALTER TABLE `test1`.`prsin_socialnetworks` DROP FOREIGN KEY `fk_persons`;
ALTER TABLE `test1`.`prsin_socialnetworks` ADD CONSTRAINT `fk_persons`  FOREIGN KEY (`person_id`)  REFERENCES `test1`.`persons` (`person_id`)  ON DELETE CASCADE;
ALTER TABLE `test1`.`prsin_socialnetworks` DROP FOREIGN KEY `fk_spr_networks`;
ALTER TABLE `test1`.`prsin_socialnetworks` ADD CONSTRAINT `fk_spr_networks`  FOREIGN KEY (`sn_id`)  REFERENCES `test1`.`sprsocialnetworks` (`sn_id`)  ON DELETE CASCADE;
