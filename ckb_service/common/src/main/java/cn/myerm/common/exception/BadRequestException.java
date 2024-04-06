package cn.myerm.common.exception;


import org.springframework.http.HttpStatus;

public class BadRequestException extends AbstractApolloHttpException {


  public BadRequestException(String str) {
    super(str);
    setHttpStatus(HttpStatus.BAD_REQUEST);
  }
}
