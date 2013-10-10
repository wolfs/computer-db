package models

trait FakeDatabaseAccess extends DatabaseAccess {
	override type Session = Int
	override def withTransaction[T](f: Session => T): T = f(0)
}

trait FakeRepositoryCompontent extends RepositoryComponent with FakeDatabaseAccess {
  var companyStore: Map[Long, Company] = Map() 
  var computerStore: Map[Long, Computer] = Map() 
  
  override val Companies: Companies = new Companies {
    def insert(model: Company)(implicit db: Session): Company = {
      val id = (companyStore.keys.max + 1)
      val modelWithId = model.copy(id = Some(id))
      companyStore += id -> modelWithId
      modelWithId
    }

    def list(implicit db: Session): Seq[Company] = {
      companyStore.values.toSeq
    }
    
  }

  override val Computers: Computers = new Computers {
	
    def insert(model: Computer)(implicit db: Session): Computer = {
      val id = (computerStore.keys.max + 1)
      val modelWithId = model.copy(id = Some(id))
      computerStore += id -> modelWithId
      modelWithId      
    }

    def findById(id: Long)(implicit db: Session): Option[Computer] = {
      computerStore.get(id)
    }
    def update(model: Computer)(implicit db: Session): Int = {
      if (computerStore.contains(model.id.get)) {
    	  computerStore += model.id.get -> model
    	  1
      } else 0
    }
    def delete(id: Long)(implicit db: Session): Int = {
      if (computerStore.contains(id)) {
        computerStore -= id
        1
      } else 0
    }
    def list(
        page: Int = 0,
        pageSize: Int = 10,
        orderBy: Int = 1,
        filter: String = "%",
        descending: Boolean = false)(implicit db: Session): Page[(Computer, Option[Company])] = {
      val sorted = computerStore.values.toSeq.filter(_.name.matches(filter.replace("%", ".*"))).sortBy(_.name)
      val items = sorted.drop(page * pageSize).take(pageSize).map {
        computer => (
            computer,
            for { id <- computer.id; company <- companyStore.get(id) } yield company)
      }
      Page(items.toList, page, page * pageSize, computerStore.size)
    }


  }

  
}