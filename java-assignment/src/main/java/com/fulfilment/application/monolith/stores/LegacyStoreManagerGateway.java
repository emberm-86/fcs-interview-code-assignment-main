package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  private static final Logger LOGGER =
          Logger.getLogger(LegacyStoreManagerGateway.class.getName());

  public void createStoreOnLegacySystem(Store store) {
    LOGGER.infof("Syncing CREATED store to legacy system. id=%d, name=%s",
            store.id, store.name);

    writeToFile("CREATED", store);
  }

  public void updateStoreOnLegacySystem(Store store) {
    LOGGER.infof("Syncing UPDATED store to legacy system. id=%d, name=%s",
            store.id, store.name);

    writeToFile("UPDATED", store);
  }

  public void deleteStoreOnLegacySystem(Long id) {
    LOGGER.infof("Syncing DELETED store to legacy system. id=%d", id);

    try {
      Path tempFile = Files.createTempFile("store-deleted-" + id, ".txt");

      String content = "Store deleted. [ id =" + id + " ]";

      Files.writeString(tempFile, content);

      LOGGER.debugf("Temporary legacy delete file created at %s",
              tempFile.toAbsolutePath());

      Files.deleteIfExists(tempFile);

    } catch (IOException e) {
      LOGGER.errorf(e,
              "Failed to sync DELETE operation to legacy system for id=%d",
              id);

      throw new RuntimeException("Legacy delete sync failed", e);
    }
  }

  private void writeToFile(String operation, Store store) {
    try {
      Path tempFile = Files.createTempFile("store-" + store.id, ".txt");

      LOGGER.debugf("Temporary file created at %s",
              tempFile.toAbsolutePath());

      String content =
              String.format(
                      "Store %s. [ id=%d ] [ name=%s ] [ items on stock=%d ]",
                      operation,
                      store.id,
                      store.name,
                      store.quantityProductsInStock
              );

      Files.writeString(tempFile, content);

      LOGGER.debug("Data written to temporary legacy file successfully");

      Files.deleteIfExists(tempFile);

      LOGGER.debug("Temporary legacy file deleted");

    } catch (IOException e) {
      LOGGER.errorf(e,
              "Failed to sync %s operation to legacy system for store id=%d",
              operation,
              store.id);

      // VERY IMPORTANT:
      // Since this runs AFTER COMMIT,
      // throwing exception won't rollback DB.
      // But it signals failure to caller / logs properly.
      throw new RuntimeException("Legacy sync failed", e);
    }
  }
}