package uk.ac.bbsrc.tgac.miso.core.service.naming.generation;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.bbsrc.tgac.miso.core.data.Library;
import uk.ac.bbsrc.tgac.miso.core.exception.MisoNamingException;
import uk.ac.bbsrc.tgac.miso.core.store.LibraryStore;

public class DefaultLibraryAliasGenerator implements NameGenerator<Library> {

  @Autowired
  private LibraryStore libraryStore;

  public void setLibraryStore(LibraryStore libraryStore) {
    this.libraryStore = libraryStore;
  }

  @Override
  public String generate(Library library) throws MisoNamingException, IOException {
    if (library.getSample() != null) {
      Pattern samplePattern = Pattern.compile("([A-z0-9]+)_S([A-z0-9]+)_(.*)");
      Matcher m = samplePattern.matcher(library.getSample().getAlias());

      if (m.matches()) {
        Collection<Library> siblings = libraryStore.listBySampleId(library.getSample().getId());
        Set<String> siblingAliases = siblings.stream()
            .map(Library::getAlias)
            .collect(Collectors.toSet());
        String alias = null;
        long siblingNumber = siblings.stream()
            .filter(sibling -> sibling.getId() != library.getId())
            .count();
        do {
          siblingNumber++;
          alias = m.group(1) + "_" + "L" + m.group(2) + "-" + siblingNumber + "_" + m.group(3);
        } while (siblingAliases.contains(alias));
        return alias;
      } else {
        throw new MisoNamingException("Cannot generate Library alias from supplied sample alias: " + library.getSample().getAlias());
      }
    } else {
      throw new NullPointerException("This alias generation scheme requires the Library to have a parent Sample set.");
    }
  }

}
