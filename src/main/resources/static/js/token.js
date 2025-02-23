const token = getAccessTokenFromCookie()

if (token) {
    localStorage.setItem("access_token", token) // ArticleList.html의 바로 밑에서 article.js가 사용함.
}

function getAccessTokenFromCookie() {
    // 현재 페이지의 모든 쿠키를 가져와서 ; 로 분리
    const cookies = document.cookie.split(';');

    // 쿠키 배열을 순회하면서 'accessToken' 찾기
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'accessToken') {
            return value;
        }
    }
    return null; // 토큰이 없는 경우
}


// localStorage.setItem("access_token" , token) 의 의미
// 이 코드는 웹 브라우저의 localStorage에 JWT 토큰을 저장하는 코드입니다. -> 그래서 구글에서 로그인 하고 edge에서 같은 사이트에 로그인 해도 정보 연동 안 됨(token을 브라우저별로 저장하니까)
//   구체적으로 설명하면:
//
//   if (token): token 값이 존재하는지 확인합니다
//
//   token이 null, undefined, 빈 문자열이 아닌 경우 조건문 내부가 실행됩니다
//
//   localStorage.setItem("access_token", token):
//
//   localStorage: 브라우저에서 제공하는 영구 저장소입니다
//   setItem(): key-value 쌍으로 데이터를 저장하는 메소드입니다
//   "access_token": 저장할 데이터의 키 이름입니다
//   token: 실제 저장할 JWT 토큰 값입니다
//
//   이렇게 저장된 토큰은 나중에 다음과 같이 불러올 수 있습니다:
//   const token = localStorage.getItem("access_token");


//괭장히 현재의 보안이 취약하다 함께 바꿔보고 싶다
//사용자가 로그인 성공 후 리다이렉트될 때 URL이 이런 형태일 것입니다:
//  https://yoursite.com/main?token=actual_access_token
//
//  이는 보안상 좋지 않은 방식입니다. 왜냐하면:
//
//  URL에 실제 액세스 토큰이 노출됨
//  브라우저 히스토리에 토큰이 남음
//  누군가 사용자의 화면/브라우저 히스토리를 볼 수 있다면 토큰 탈취 가능