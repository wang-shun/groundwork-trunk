      <div class="modal-header">
        <button type="button" class="close" aria-hidden="true" ng-click="close()">&times;</button>
        <h4 class="modal-title">Delete Host Identities</h4>
      </div>
      <form role="form" novalidate class="css-form">
      <div class="modal-body" wo-augment-dialog>
          <p class="text-danger">Are you sure you want to delete all the selected host identities?</p>
          <p class="text-danger">This cannot be undone.</p>
      </div>
      <div class="modal-footer">
        <button class="btn btn-default" ng-click="close()">Cancel</button>
        <button type="submit" class="btn btn-primary" ng-click="delete()">Delete</button>
      </div>
      </form>
